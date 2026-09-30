# Working with the Supabase backend

This document describes how the Jervis Supabase project is configured, and in
particular how transactional email for email/password authentication is sent
through [Resend](https://resend.com).

The project configuration lives in [`serverTools/supabase`](../serverTools/supabase).
The hosted project is called "Jervis Server" (`mrqduvlohcxijeispsix`, region
`eu-north-1`).

`config.toml` is the source of truth. Do not edit auth settings in the Supabase
dashboard, they will be overwritten the next time someone runs
`supabase config push`.


## Requirements

- The Supabase CLI: `brew install supabase/tap/supabase`
- Docker, if you want to run the local stack with `supabase start`
- A Resend account with access to the `ilios.dk` DNS zone


## How configuration is applied

`config.toml` is read by two different commands, and they do not see the same
settings:

| Command | What it configures | Which settings apply |
| --- | --- | --- |
| `supabase start` | The local Docker stack | Everything except `[remotes.*]` |
| `supabase config push` | The hosted project | The same, with `[remotes.production]` merged on top |

The `[remotes.production]` block is matched by `project_id`, so it only applies
when pushing to `mrqduvlohcxijeispsix`. This is what keeps production secrets
and production-only behaviour out of the local stack.

Secrets are never written to `config.toml`. Values are referenced as
`env(NAME)` and resolved from the shell environment, `serverTools/supabase/.env`
or `serverTools/supabase/.env.local`. Both `.env` files are ignored by Git;
copy [`.env.example`](../serverTools/supabase/.env.example) to `.env.local` and
fill it in.

All CLI commands below are run from `serverTools`, since the CLI looks for a
directory named `supabase` below the working directory. From anywhere else, add
`--workdir serverTools`.


## Email

Supabase ships with a built-in email service, but it is only meant for getting
started: it is rate limited to a couple of mails per hour and delivery to
addresses outside the Supabase organisation is not guaranteed. Anything
user-facing needs a real SMTP server, so password recovery, email change
confirmations and (once enabled) sign-up confirmations all go through Resend.

### Verify the sending domain

In the Resend dashboard, go to **Domains > Add Domain** and add
`jervis.ilios.dk`. Pick the region closest to the users, which for us is the
EU one. Using a subdomain instead of `ilios.dk` keeps the sending reputation of
the app separate from any other mail sent from the apex domain.

Resend then lists the DNS records to add to the `ilios.dk` zone. Copy the
values from the dashboard, but they look like this:

| Type | Name | Value |
| --- | --- | --- |
| MX | `send.jervis` | `feedback-smtp.eu-west-1.amazonses.com` (priority 10) |
| TXT | `send.jervis` | `v=spf1 include:amazonses.com ~all` |
| TXT | `resend._domainkey.jervis` | `p=MIGfMA0GCSqGSIb3DQEB...` |
| TXT | `_dmarc.jervis` | `v=DMARC1; p=none;` |

Note that the names are relative to the zone. If your DNS provider wants fully
qualified names, use `send.jervis.ilios.dk` and so on.

Press **Verify** in Resend afterwards. Propagation is usually a few minutes,
but can take hours. Do not continue until the domain shows up as verified,
Resend rejects any mail sent from an unverified domain.

### Create an API key

Go to **API Keys > Create API Key**. Give it `Sending access` only, and
restrict it to the `jervis.ilios.dk` domain. The key is only shown once, so
copy it right away.

Resend uses the API key as the SMTP password, so the key is all that is needed:

| Setting | Value |
| --- | --- |
| Host | `smtp.resend.com` |
| Port | `465` (implicit TLS). `587` with STARTTLS also works |
| Username | `resend` |
| Password | The `re_...` API key |

Store the key in `serverTools/supabase/.env.local`:

```text
RESEND_SMTP_PASSWORD=re_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

### Push the configuration

The SMTP settings themselves are already in `config.toml` under
`[remotes.production.auth.email.smtp]`. They only need to be pushed:

```shell
# Only needed once per machine
supabase link --project-ref mrqduvlohcxijeispsix

supabase config push
```

`config push` prints the changes it is about to make. Read them before
confirming, since it pushes *all* of `config.toml` and not just the mail
settings. Afterwards, **Authentication > Emails > SMTP Settings** in the
dashboard should show `smtp.resend.com`.

If `RESEND_SMTP_PASSWORD` is not set, the password is pushed as an empty string
and every mail fails with an authentication error, so make sure `.env.local`
exists before pushing.

### Verify that mail is delivered

Trigger a password recovery mail for an existing user:

```shell
curl -X POST 'https://mrqduvlohcxijeispsix.supabase.co/auth/v1/recover' \
  -H 'apikey: <publishable key>' \
  -H 'Content-Type: application/json' \
  -d '{"email":"you@example.com"}'
```

The same thing can be done from the Login dialog in the app, which calls
`JervisAuthService.sendPasswordResetEmail()`.

Two places to look when it does not arrive:

- **Resend > Emails** shows every message Resend accepted, including bounces
  and spam complaints. If the message is not here, it never left Supabase.
- **Supabase dashboard > Logs > Auth** shows SMTP errors from the Auth service.


## Local development

`supabase start` ignores `[remotes.production]`, so local auth mail is captured
by the bundled Mailpit server instead of being sent anywhere:

```shell
supabase start
open http://localhost:54324
```

That is the intended setup for day to day work. It costs no Resend quota and
cannot mail a real person by accident.

To test the real Resend path locally, uncomment the `[auth.email.smtp]` block
in `config.toml` and restart the stack. Remember to comment it back out, and
keep `admin_email` on `jervis.ilios.dk`.


## Rate limits

Two separate limits apply to outgoing mail, both set in `[remotes.production]`:

- `auth.email.max_frequency` (`60s`) is the cooldown before the same address
  can be mailed again.
- `auth.rate_limit.email_sent` (`30`) is how many mails the project may send
  per hour in total.

The local stack keeps the much looser defaults (`1s` and `2`) from the top of
`config.toml`.

Resend's free tier has its own daily and monthly caps on top of this. Check the
current numbers on their pricing page before assuming the Supabase limits are
the binding ones.


## Sign-up confirmation

`auth.email.enable_confirmations` is currently `false`, so a sign-up creates a
usable account immediately and no confirmation mail is sent. Now that a real
SMTP server is configured, this can be turned on by adding it to the remote
overrides in `config.toml`:

```toml
[remotes.production.auth.email]
enable_confirmations = true
```

The client is already prepared for it: `signUpWithEmail()` returns
`SignUpOutcome.ConfirmationRequired` when the new user is not signed in
afterwards.

The confirmation link points at `/auth/v1/verify` on the Supabase project,
which marks the user as confirmed and then redirects to `auth.site_url`
(`https://jervis.ilios.dk`). Confirmation therefore works without any in-app
handling, the user just has to go back to the app and sign in.

Password recovery links are a different story. Jervis can request them, but
cannot complete them inside the app yet, so recovery is not usable end to end
regardless of the mail setup.


## Email templates

Supabase sends its own default templates. To replace them, uncomment the
`[auth.email.template.*]` blocks in `config.toml` and point `content_path` at
an HTML file. Templates are pushed by `supabase config push` like everything
else.
