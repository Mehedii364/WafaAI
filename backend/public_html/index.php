<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Wafa AI - Intelligent Assistant Gateway</title>
    <style>
        :root {
            --bg: #0a0f1d;
            --surface: #0f172a;
            --border: #1e293b;
            --cyan: #06b6d4;
            --indigo: #6366f1;
            --text: #f8fafc;
            --muted: #94a3b8;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            background: var(--bg);
            color: var(--text);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .card {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: 20px;
            padding: 40px;
            max-width: 520px;
            width: 100%;
            text-align: center;
            box-shadow: 0 10px 40px rgba(6, 182, 212, 0.1);
        }
        .badge {
            display: inline-block;
            background: rgba(6, 182, 212, 0.15);
            color: var(--cyan);
            padding: 6px 14px;
            border-radius: 50px;
            font-size: 12px;
            font-weight: 600;
            margin-bottom: 20px;
        }
        h1 {
            font-size: 28px;
            font-weight: 700;
            margin-bottom: 8px;
            background: linear-gradient(135deg, var(--cyan), var(--indigo));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        p { color: var(--muted); line-height: 1.6; margin-bottom: 24px; font-size: 15px; }
        .info-box {
            background: #060a15;
            border-radius: 12px;
            padding: 16px;
            text-align: left;
            font-family: monospace;
            font-size: 13px;
            color: #cbd5e1;
            margin-bottom: 24px;
        }
        .btn {
            display: inline-block;
            background: linear-gradient(135deg, var(--cyan), var(--indigo));
            color: #000;
            font-weight: 600;
            padding: 12px 24px;
            border-radius: 10px;
            text-decoration: none;
            transition: opacity 0.2s;
        }
        .btn:hover { opacity: 0.9; }
        .footer { font-size: 12px; color: var(--muted); margin-top: 30px; }
    </style>
</head>
<body>
    <div class="card">
        <div class="badge">PROD GATEWAY ONLINE</div>
        <h1>Wafa AI</h1>
        <p>Production AI Gateway &amp; Android Companion Service with 10-Key Rotation and Automatic Failover.</p>
        <div class="info-box">
            <div><strong>Status:</strong> Active</div>
            <div><strong>Endpoint:</strong> /api/chat.php</div>
            <div><strong>Health:</strong> /api/health.php</div>
            <div><strong>Model:</strong> OpenRouter Multi-Key Proxy</div>
        </div>
        <a href="api/health.php" class="btn">Check Gateway Health</a>
        <div class="footer">
            Developed by <strong>Mehedi364</strong> &bull; Wafa AI v1.0
        </div>
    </div>
</body>
</html>
