<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

echo json_encode([
    'status' => 'online',
    'app' => 'Wafa AI Gateway',
    'version' => '1.0',
    'developer' => 'Mehedi364',
    'timestamp' => time()
]);
