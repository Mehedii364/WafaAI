# Wafa AI - InfinityFree Deployment & Custom Domain Guide

This directory contains the production-ready PHP backend for **Wafa AI**, designed to run on **InfinityFree Shared Hosting** with MySQL/MariaDB and Apache.

---

## 1. Hosting Requirements

- **PHP**: 8.0, 8.1, or 8.2 (standard on InfinityFree)
- **Database**: MySQL / MariaDB (provided free by InfinityFree)
- **cPanel / File Manager / FTP**: Supported
- **Domain**: Custom domain or free sub-domain (`.epizy.com`) with HTTPS (Free SSL via InfinityFree or Cloudflare)

---

## 2. Directory Structure to Upload

Upload the contents of `public_html/` to your InfinityFree account root:
```text
htdocs/ (or public_html/)
├── index.php
├── .htaccess
├── config.example.php  -> copy to config.php
├── database.sql
└── api/
    ├── chat.php
    └── health.php
```

---

## 3. Step-by-Step Installation

### Step 1: Create Database
1. Log in to your InfinityFree Control Panel (cPanel).
2. Go to **MySQL Databases**.
3. Create a new database, for example: `epiz_XXXX_wafaai`.
4. Note your MySQL Details:
   - **MySQL Host**: e.g., `sqlXXX.epizy.com`
   - **MySQL Database**: `epiz_XXXX_wafaai`
   - **MySQL Username**: `epiz_XXXX`
   - **MySQL Password**: (Your vPanel password)

### Step 2: Import SQL Database
1. Open **phpMyAdmin** from the InfinityFree Control Panel.
2. Select your newly created database.
3. Click the **Import** tab.
4. Upload `database.sql` and click **Go**.

### Step 3: Configure `config.php`
1. In File Manager or via FTP, rename `config.example.php` to `config.php`.
2. Fill in your MySQL credentials:
```php
'db' => [
    'host' => 'sqlXXX.epizy.com',
    'name' => 'epiz_XXXX_wafaai',
    'user' => 'epiz_XXXX',
    'pass' => 'your_password',
    'charset' => 'utf8mb4'
]
```

### Step 4: Connect with Wafa AI Android App
1. Open **Wafa AI** on your Android device.
2. Go to **Settings** > **PHP Backend Gateway (Optional)**.
3. Enter your domain: `https://yourdomain.com` (leave blank if connecting directly to OpenRouter).

---

## 4. Security Highlights
- No API keys are committed or visible in public client code.
- `.htaccess` blocks public access to `config.php`, `.sql`, and dotfiles.
- HTTPS is automatically enforced.
- Prepared PDO statements are used throughout.
