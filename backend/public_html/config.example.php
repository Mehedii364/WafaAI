<?php
/**
 * Wafa AI - Backend Configuration File Template
 * Rename this file to config.php on your InfinityFree / Custom Domain hosting.
 */

return [
    'app_name' => 'Wafa AI Gateway',
    'app_url' => 'https://yourdomain.com',
    'app_env' => 'production', // 'production' or 'development'

    // Database Configuration (InfinityFree MySQL credentials)
    'db' => [
        'host' => 'sqlXXX.epizy.com', // Replace with your InfinityFree MySQL host
        'name' => 'epiz_XXXXXXXX_wafaai',
        'user' => 'epiz_XXXXXXXX',
        'pass' => 'YOUR_DB_PASSWORD',
        'charset' => 'utf8mb4'
    ],

    // Default OpenRouter configuration (Used as server-side fallback or proxy)
    'openrouter' => [
        'base_url' => 'https://openrouter.ai/api/v1',
        'default_model' => 'google/gemini-2.0-flash-001',
        // Optional server-side master key if you wish to run a shared gateway:
        'master_api_key' => ''
    ],

    // Security Settings
    'security' => [
        'allowed_origins' => [
            '*' // Or specify your client app origin
        ],
        'rate_limit_per_minute' => 60
    ]
];
