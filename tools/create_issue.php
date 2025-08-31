<?php
/**
 * Small PHP script that act as a REST API endpoint for creating GitHub issues.
 * This is used as a proxy for the Jervis Client to avoid issues with people not having GitHub accounts, so
 * instead of the user having to create it. This responsibility is delegated to the "Jervis Support Bot" GitHub App.
 *
 * GitHub does not allow attaching files to issues programmatically, so they are uploaded to the website instead
 * and then a link is inserted into the issue body. A high amount traffic is not expected, so this is probably fine.
 */
declare(strict_types=1);

/**
 * ---- Config ----
 */
$GITHUB_APP_ID = getenv('GITHUB_APP_ID') ?: 'YOUR_APP_ID';
$GITHUB_CLIENT_ID = getenv('GITHUB_CLIENT_ID') ?: 'YOUR_CLIENT_ID';
$GITHUB_INSTALLATION_ID = getenv('GITHUB_INSTALLATION_ID') ?: 'YOUR_INSTALLATION_ID';
$GITHUB_OWNER = getenv('GITHUB_OWNER') ?: 'owner';
$GITHUB_REPO = getenv('GITHUB_REPO')  ?: 'repo';
$GITHUB_APP_PRIVATE_KEY_B64 = getenv('GITHUB_APP_PRIVATE_KEY_B64') ?: 'JERVIS_SUPPORT_BOT_PRIVATE_KEY';

/* ---------- Helpers ---------- */
function h(string $s): string { return htmlspecialchars($s, ENT_QUOTES | ENT_SUBSTITUTE | ENT_HTML5, 'UTF-8'); }
function respond(int $status, array $data): never {
    http_response_code($status);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode($data, JSON_UNESCAPED_SLASHES);
    exit;
}
function respond_error(int $status, string $msg): never {
    respond($status, ["type" => "error", "message" => $msg]);
}
function respond_success(string $msg): never {
    respond(200, ["type" => "success", "message" => $msg]);
}
function base_url(): string {
    $https = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') || (($_SERVER['SERVER_PORT'] ?? null) === '443');
    $scheme = $https ? 'https' : 'http';
    return $scheme.'://'.$_SERVER['HTTP_HOST'];
}
function upload_dir(): array {
    $dir = __DIR__ . '/uploads';
    if (!is_dir($dir)) @mkdir($dir, 0775, true);
    $scriptDir = rtrim(dirname($_SERVER['SCRIPT_NAME'] ?? ''), '/');
    $url = rtrim(base_url().$scriptDir, '/').'/uploads';
    return [$dir, $url];
}
function sanitize_name(string $name): string {
    $name = preg_replace('/[^A-Za-z0-9._-]/', '_', $name) ?? '';
    return trim($name, '._-') ?: ('file_'.bin2hex(random_bytes(4)));
}
function check_post_value(string $name): string {
    if (array_key_exists($name, $_POST)) {
        return trim((string)$_POST[$name]);
    } else {
        respond_error(400, "Missing $name");
    }
}

/* ---------- GitHub App Auth ---------- */
/** Create a JWT for the GitHub App (RS256). */
function github_app_jwt(string $clientId, string $privateKeyBase64): string {
    $now = time();
    $payload = [
            'iat' => $now - 60,           // clock skew
            'exp' => $now + 540,          // max 10 minutes
            'iss' => $clientId,
    ];
    $segments = [
            rtrim(strtr(base64_encode(json_encode(['alg'=>'RS256','typ'=>'JWT'])), '+/', '-_'), '='),
            rtrim(strtr(base64_encode(json_encode($payload, JSON_UNESCAPED_SLASHES)), '+/', '-_'), '='),
    ];
    $signingInput = implode('.', $segments);
    $privateKeyPem = base64_decode($privateKeyBase64);
    if ($privateKeyPem === false) respond_error(400,"Invalid private key (base64_decode failed)");
    $pkey = openssl_pkey_get_private($privateKeyPem);
    if ($pkey === false) throw new RuntimeException('Invalid private key (openssl_pkey_get_private failed)');
    $ok = openssl_sign($signingInput, $sig, $pkey, OPENSSL_ALGO_SHA256);
    openssl_pkey_free($pkey);
    if (!$ok) respond_error(400, "openssl_sign failed");

    $segments[] = rtrim(strtr(base64_encode($sig), '+/', '-_'), '=');
    return implode('.', $segments);
}

/** Exchange App JWT for an installation access token. */
function github_installation_token(string $jwt, string $installationId): array {
    $url = "https://api.github.com/app/installations/".rawurlencode($installationId)."/access_tokens";
    $ch = curl_init($url);
    curl_setopt_array($ch, [
            CURLOPT_POST => true,
            CURLOPT_POSTFIELDS => "{}", // empty body OK
            CURLOPT_HTTPHEADER => [
                    'Accept: application/vnd.github+json',
                    'Authorization: Bearer '.$jwt,
                    'User-Agent: IssueReporter',
                    'X-GitHub-Api-Version: 2022-11-28',
                    'Content-Type: application/json',
            ],
            CURLOPT_RETURNTRANSFER => true,
    ]);
    $resp = curl_exec($ch);
    $status = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $err = curl_error($ch);
    curl_close($ch);
    return [$status ?: 0, $resp ?: '', $err ?: null];
}

/** Create an issue using the installation token. */
function create_issue_with_installation_token(string $instToken, string $owner, string $repo, string $title, string $body): array {
    $ch = curl_init("https://api.github.com/repos/$owner/$repo/issues");
    $payload = json_encode(['title'=>$title, 'body'=>$body, 'labels' => ['user']], JSON_UNESCAPED_SLASHES);
    curl_setopt_array($ch, [
            CURLOPT_POST => true,
            CURLOPT_POSTFIELDS => $payload,
            CURLOPT_HTTPHEADER => [
                    'Accept: application/vnd.github+json',
                    'Content-Type: application/json; charset=utf-8',
                    'Authorization: Bearer '.$instToken,
                    'User-Agent: IssueReporter',
                    'X-GitHub-Api-Version: 2022-11-28',
            ],
            CURLOPT_RETURNTRANSFER => true,
    ]);
    $resp = curl_exec($ch);
    $status = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $err = curl_error($ch);
    curl_close($ch);
    return [$status ?: 0, $resp ?: '', $err ?: null];
}

/* ---------- Session ---------- */
$_SESSION['title'] = $_SESSION['title'] ?? '';
$_SESSION['body']  = $_SESSION['body']  ?? '';
$_SESSION['files'] = $_SESSION['files'] ?? [];


/* ---------- POST handling ---------- */
if (($_SERVER['REQUEST_METHOD'] ?? '') != 'POST') {
    respond_error(405, "Only POST requests are allowed");
} else {
    // Check required inputs
    $title = check_post_value('title');
    $body = check_post_value('body');

    // Make sure we have a valid Github Access Token before uploading files
    $jwt  = github_app_jwt($GITHUB_CLIENT_ID, $GITHUB_APP_PRIVATE_KEY_B64);
    [$st1, $resp1, $err1] = github_installation_token($jwt, $GITHUB_INSTALLATION_ID);
    if ($err1) respond_error(400, "Install token request failed: $err1");
    $data1 = json_decode($resp1, true);
    if ($st1 < 200 || $st1 >= 300 || !is_array($data1) || empty($data1['token'])) {
        $msg = is_array($data1) && isset($data1['message']) ? $data1['message'] : "HTTP $st1";
        respond_error(500, "Install token error: $msg");
    }
    $instToken = (string)$data1['token'];

    // Upload files (if any)
    $files = [];
    if (!empty($_FILES['attachments']['name'][0])) {
        [$dir, $urlBase] = upload_dir();
        $names = $_FILES['attachments']['name'];
        $errors = $_FILES['attachments']['error'];
        $tmps = $_FILES['attachments']['tmp_name'];

        foreach ($names as $i => $orig) {
            $err = (int)($errors[$i] ?? UPLOAD_ERR_NO_FILE);
            if ($err !== UPLOAD_ERR_OK) continue;
            $tmp = (string)($tmps[$i] ?? '');
            if ($tmp === '' || !is_uploaded_file($tmp)) continue;

            $safe = sanitize_name((string)$orig);
            $ext  = pathinfo($safe, PATHINFO_EXTENSION);
            $name = pathinfo($safe, PATHINFO_FILENAME);
            $final = $name.'-'.date('Ymd-His').'-'.bin2hex(random_bytes(3)).($ext ? ".$ext" : '');
            $dest = $dir.'/'.$final;

            if (move_uploaded_file($tmp, $dest)) {
                $files[] = ['name'=>(string)$orig, 'url'=>"$urlBase/$final", 'path'=>$dest];
            }
        }
    }
    if (!empty($files)) {
        $body .= "\n\n**Attachments**\n";
        foreach ($files as $f) $body .= "- [".($f['name'])."](".$f['url'].")\n";
    }

    // Create issue on Github using the Jervis Support Bot as author
    [$st2, $resp2, $err2] = create_issue_with_installation_token($instToken, $GITHUB_OWNER, $GITHUB_REPO, $title, $body);
    if ($err2) respond(500, ["error" => "Create issue cURL error: $err2"]);
    $data2 = json_decode($resp2, true);
    if ($st2 >= 200 && $st2 < 300 && is_array($data2) && isset($data2['html_url'])) {
        $issueUrl = (string)$data2['html_url'];
        respond_success($issueUrl);
    } else {
        $msg = is_array($data2) && isset($data2['message']) ? (string)$data2['message'] : ("HTTP ".$st2);
        $details = (is_array($data2) && !empty($data2['errors'])) ? " (details: ".h(json_encode($data2['errors'], JSON_UNESCAPED_SLASHES)).")" : "";
        respond_error(500, "Failed to create issue: $msg$details");
    }
}