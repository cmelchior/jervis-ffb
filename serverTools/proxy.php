<?php
/**
 * Proxy used by Jervis to work around various CORS issues.
 * Is also used to convert AVIF images (from TourPlay) to PNG so we avoid
 * having to bundle an explicit decoder with Compose.
 * See https://youtrack.jetbrains.com/projects/CMP/issues/CMP-7639/Add-support-for-avif-images
 */
declare(strict_types=1);

ini_set('display_errors', '1');
ini_set('zlib.output_compression', '0');

ob_start();

function clearOutputBuffers(): void {
    while (ob_get_level() > 0) {
        ob_end_clean();
    }
}

function sendError(int $statusCode, string $message): never {
    clearOutputBuffers();

    http_response_code($statusCode);
    header('Content-Type: text/plain; charset=utf-8');
    header('Cache-Control: no-store');

    echo $message;
    exit;
}

function sendResponse(
    string $data,
    string $contentType,
    int $statusCode = 200,
    array $headers = []
): never {
    clearOutputBuffers();

    http_response_code($statusCode);
    header('Content-Type: ' . $contentType);
    header('Content-Length: ' . strlen($data));

    foreach ($headers as $header) {
        header($header);
    }

    echo $data;
    exit;
}

// -----------------------------------------------------------------------------
// CORS
// -----------------------------------------------------------------------------

$origin = isset($_SERVER['HTTP_ORIGIN'])
    ? rtrim($_SERVER['HTTP_ORIGIN'], '/')
    : '';

$originAllowed =
    preg_match('#^http://localhost(:\d+)?$#', $origin) === 1 ||
    $origin === 'https://jervis.ilios.dk';

if ($originAllowed) {
    header("Access-Control-Allow-Origin: $origin");
    header('Vary: Origin');
} elseif ($origin !== '') {
    sendError(403, "CORS not allowed for this origin: $origin");
}

$requestMethod = $_SERVER['REQUEST_METHOD'] ?? 'GET';

if ($requestMethod === 'OPTIONS') {
    clearOutputBuffers();

    header('Access-Control-Allow-Methods: GET, HEAD, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type, Range');
    header('Access-Control-Max-Age: 86400');

    http_response_code(204);
    exit;
}

if ($requestMethod !== 'GET' && $requestMethod !== 'HEAD') {
    sendError(405, 'Only GET, HEAD and OPTIONS requests are supported');
}

// -----------------------------------------------------------------------------
// Validate target URL
// -----------------------------------------------------------------------------

if (!isset($_GET['url']) || !is_string($_GET['url'])) {
    sendError(400, "Missing 'url' parameter");
}

$url = trim($_GET['url']);

if (!filter_var($url, FILTER_VALIDATE_URL)) {
    sendError(400, 'Invalid URL');
}

$scheme = strtolower((string) parse_url($url, PHP_URL_SCHEME));

if ($scheme !== 'http' && $scheme !== 'https') {
    sendError(400, 'Only HTTP and HTTPS URLs are supported');
}

// -----------------------------------------------------------------------------
// Fetch upstream resource
// -----------------------------------------------------------------------------

$curl = curl_init($url);

if ($curl === false) {
    sendError(500, 'Failed to initialize cURL');
}

$upstreamHeaders = [];

curl_setopt_array($curl, [
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_FOLLOWLOCATION => true,
    CURLOPT_MAXREDIRS => 5,
    CURLOPT_CONNECTTIMEOUT => 10,
    CURLOPT_TIMEOUT => 30,
    CURLOPT_USERAGENT => 'Jervis Resource Proxy/1.0',

    CURLOPT_PROTOCOLS => CURLPROTO_HTTP | CURLPROTO_HTTPS,
    CURLOPT_REDIR_PROTOCOLS => CURLPROTO_HTTP | CURLPROTO_HTTPS,

    CURLOPT_FAILONERROR => false,

    CURLOPT_HTTPHEADER => [
        'Accept: */*',
    ],

    CURLOPT_HEADERFUNCTION => static function (
        CurlHandle $curl,
        string $header
    ) use (&$upstreamHeaders): int {
        $length = strlen($header);
        $parts = explode(':', $header, 2);

        if (count($parts) === 2) {
            $name = strtolower(trim($parts[0]));
            $value = trim($parts[1]);
            $upstreamHeaders[$name] = $value;
        }

        return $length;
    },
]);

// Forward the client's Range request when present.
if (isset($_SERVER['HTTP_RANGE'])) {
    curl_setopt(
        $curl,
        CURLOPT_HTTPHEADER,
        [
            'Accept: */*',
            'Range: ' . $_SERVER['HTTP_RANGE'],
        ]
    );
}

$data = curl_exec($curl);

if ($data === false) {
    $error = curl_error($curl);
    curl_close($curl);

    sendError(502, "Failed to fetch resource: $error");
}

$statusCode = (int) curl_getinfo($curl, CURLINFO_RESPONSE_CODE);
$contentType = curl_getinfo($curl, CURLINFO_CONTENT_TYPE);

curl_close($curl);

$contentType = is_string($contentType)
    ? strtolower(trim(explode(';', $contentType)[0]))
    : '';

if ($statusCode < 200 || $statusCode >= 400) {
    sendError(502, "Upstream server returned HTTP $statusCode");
}

// -----------------------------------------------------------------------------
// Convert only explicit AVIF responses to PNG
// -----------------------------------------------------------------------------

if ($contentType === 'image/avif') {
    $inputFile = tempnam(sys_get_temp_dir(), 'jervis_avif_');
    $outputFile = tempnam(sys_get_temp_dir(), 'jervis_png_');

    if ($inputFile === false || $outputFile === false) {
        if (is_string($inputFile)) {
            @unlink($inputFile);
        }

        if (is_string($outputFile)) {
            @unlink($outputFile);
        }

        sendError(500, 'Failed to create temporary files');
    }

    try {
        if (file_put_contents($inputFile, $data) === false) {
            sendError(500, 'Failed to write temporary AVIF file');
        }

        $image = @imagecreatefromavif($inputFile);

        if ($image === false) {
            sendError(502, 'Failed to decode AVIF image');
        }

        imagealphablending($image, false);
        imagesavealpha($image, true);

        $encoded = @imagepng($image, $outputFile, 6);
        imagedestroy($image);

        if (!$encoded) {
            sendError(500, 'Failed to encode PNG image');
        }

        $pngData = file_get_contents($outputFile);

        if (
            $pngData === false ||
            !str_starts_with($pngData, "\x89PNG\r\n\x1A\n")
        ) {
            sendError(500, 'Generated output is not a valid PNG');
        }

        $headers = [
            // 'Cache-Control: public, max-age=86400',
            'X-Image-Converted: avif-to-png',
        ];

        if (isset($upstreamHeaders['etag'])) {
            // The upstream ETag describes the AVIF, not the generated PNG.
            $headers[] = 'X-Upstream-ETag: ' . $upstreamHeaders['etag'];
        }

        if ($requestMethod === 'HEAD') {
            sendResponse('', 'image/png', 200, $headers);
        }

        sendResponse($pngData, 'image/png', 200, $headers);
    } finally {
        @unlink($inputFile);
        @unlink($outputFile);
    }
}

// -----------------------------------------------------------------------------
// Forward every other resource unchanged
// -----------------------------------------------------------------------------

$responseContentType = $contentType !== ''
    ? $contentType
    : 'application/octet-stream';

$responseHeaders = [
    'X-Image-Converted: no',
];

foreach ([
    'cache-control',
    'content-encoding',
    'content-disposition',
    'accept-ranges',
    'content-range',
    'etag',
    'last-modified',
] as $headerName) {
    if (isset($upstreamHeaders[$headerName])) {
        $responseHeaders[] =
            implode('-', array_map('ucfirst', explode('-', $headerName))) .
            ': ' .
            $upstreamHeaders[$headerName];
    }
}

// if (!isset($upstreamHeaders['cache-control'])) {
//     $responseHeaders[] = 'Cache-Control: public, max-age=86400';
// }

if ($requestMethod === 'HEAD') {
    sendResponse('', $responseContentType, $statusCode, $responseHeaders);
}

sendResponse(
    $data,
    $responseContentType,
    $statusCode,
    $responseHeaders
);