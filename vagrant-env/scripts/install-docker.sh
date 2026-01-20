#!/usr/bin/env bash
set -e

echo "=== docker install script"

if command -v docker &>/dev/null; then
  echo "docker already installed"
  exit 0
fi

curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker vagrant

echo "=== docker install done"
