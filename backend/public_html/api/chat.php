<?php
/**
 * Wafa AI Gateway - Chat Completions Proxy
 * Handles OpenRouter forwarding, failover headers, and streaming.
 */

header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Authorization, Content-Type, HTTP-Referer, X-Title');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => ['message' => 'Method Not Allowed']]);
    exit;
}

$rawInput = file_get_contents('php://input');
$requestData = json_decode($rawInput, true);

if (!$requestData || !isset($requestData['messages'])) {
    http_response_code(400);
    echo json_encode(['error' => ['message' => 'Invalid request payload. Missing messages.']]);
    exit;
}

// Extract Authorization Bearer key
$authHeader = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
$apiKey = '';
if (preg_match('/Bearer\s+(.*)$/i', $authHeader, $matches)) {
    $apiKey = trim($matches[1]);
}

// Fallback to config file if client key is not provided
$configFile = __DIR__ . '/../config.php';
if (empty($apiKey) && file_exists($configFile)) {
    $config = require $configFile;
    $apiKey = $config['openrouter']['master_api_key'] ?? '';
}

if (empty($apiKey)) {
    http_response_code(401);
    echo json_encode(['error' => ['message' => 'No API Key provided. Please configure OpenRouter key in Wafa AI settings.']]);
    exit;
}

$isStream = !empty($requestData['stream']);
$openRouterUrl = 'https://openrouter.ai/api/v1/chat/completions';

$headers = [
    'Content-Type: application/json',
    'Authorization: Bearer ' . $apiKey,
    'HTTP-Referer: https://wafazone.site',
    'X-Title: Wafa AI Gateway by Mehedi364'
];

$ch = curl_init($openRouterUrl);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, $rawInput);
curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, !$isStream);
curl_setopt($ch, CURLOPT_TIMEOUT, 90);
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);

if ($isStream) {
    header('Content-Type: text/event-stream');
    header('Cache-Control: no-cache');
    header('Connection: keep-alive');

    curl_setopt($ch, CURLOPT_WRITEFUNCTION, function($curl, $data) {
        echo $data;
        if (ob_get_level() > 0) {
            ob_flush();
        }
        flush();
        return strlen($data);
    });
}

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

if (curl_errno($ch)) {
    $errorMsg = curl_error($ch);
    curl_close($ch);
    http_response_code(502);
    echo json_encode(['error' => ['message' => 'Gateway Connection Error: ' . $errorMsg]]);
    exit;
}

curl_close($ch);

if (!$isStream) {
    http_response_code($httpCode);
    header('Content-Type: application/json');
    echo $response;
}
