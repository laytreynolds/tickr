#!/usr/bin/env bash
set -euo pipefail

DOMAIN="${DOMAIN:-}"
EMAIL="${EMAIL:-}"
REPO_DIR="${REPO_DIR:-$(pwd)}"
COMPOSE_FILE="${COMPOSE_FILE:-compose.yml}"
REPO_URL="${REPO_URL:-}"

if [[ -z "$DOMAIN" || -z "$EMAIL" ]]; then
  echo "Usage: DOMAIN=your.subdomain.com EMAIL=you@example.com [REPO_URL=git@...] ./deploy.sh"
  exit 1
fi

if [[ -n "$REPO_URL" && ! -d "$REPO_DIR/.git" ]]; then
  sudo mkdir -p "$REPO_DIR"
  sudo chown "$USER":"$USER" "$REPO_DIR"
  git clone "$REPO_URL" "$REPO_DIR"
fi

cd "$REPO_DIR"

if ! command -v docker >/dev/null 2>&1; then
  sudo apt update
  sudo apt install -y docker.io docker-compose-plugin
fi

sudo systemctl enable --now docker

if command -v docker compose >/dev/null 2>&1; then
  :
else
  sudo apt update
  sudo apt install -y docker-compose-plugin
fi

if ! id -nG "$USER" | grep -qw docker; then
  sudo usermod -aG docker "$USER"
  echo "Added $USER to docker group. Log out and back in to take effect."
fi

DOCKER=(docker)
if [[ "$(id -u)" -ne 0 ]] && ! id -nG "$USER" | grep -qw docker; then
  DOCKER=(sudo docker)
fi

if command -v ufw >/dev/null 2>&1 && sudo ufw status | grep -q "Status: active"; then
  sudo ufw allow 80/tcp
  sudo ufw allow 443/tcp
fi

if sudo ss -ltnp | grep -q ":80 "; then
  echo "Port 80 is already in use. Ensure no other service binds to 80."
fi

if [[ ! -f ".env" ]]; then
  echo "Missing .env in $REPO_DIR. Create it before continuing."
  exit 1
fi

mkdir -p nginx/conf.d nginx/letsencrypt nginx/webroot

cat > nginx/conf.d/tickr.conf <<EOF
server {
    listen 80;
    server_name ${DOMAIN};

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location / {
        proxy_pass http://app:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
    }
}
EOF

cat > nginx/conf.d/tickr-https.conf.example <<EOF
server {
    listen 80;
    server_name ${DOMAIN};

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location / {
        return 301 https://\$host\$request_uri;
    }
}

server {
    listen 443 ssl;
    http2 on;
    server_name ${DOMAIN};

    ssl_certificate /etc/letsencrypt/live/${DOMAIN}/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/${DOMAIN}/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers off;

    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Content-Type-Options nosniff;
    add_header X-Frame-Options DENY;
    add_header Referrer-Policy no-referrer;

    location / {
        proxy_pass http://app:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
    }
}
EOF

"${DOCKER[@]}" compose -f "$COMPOSE_FILE" up -d nginx

"${DOCKER[@]}" compose -f "$COMPOSE_FILE" run --rm --entrypoint certbot certbot certonly \
  --webroot -w /var/www/certbot \
  -d "$DOMAIN" \
  --email "$EMAIL" --agree-tos --no-eff-email

cp nginx/conf.d/tickr-https.conf.example nginx/conf.d/tickr.conf
"${DOCKER[@]}" compose -f "$COMPOSE_FILE" exec nginx nginx -s reload

"${DOCKER[@]}" compose -f "$COMPOSE_FILE" up -d

echo "Deployment complete. HTTPS should be live on https://${DOMAIN}"
