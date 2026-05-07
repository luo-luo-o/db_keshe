package pers.luoluo.databasekeshe.common.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping(value = "/api", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("""
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>PSM-Smart Backend</title>
                </head>
                <body>
                    <h1>PSM-Smart Backend is running</h1>
                    <p>Backend API service is running.</p>
                    <ul>
                        <li>Login API: POST /api/auth/login</li>
                        <li>Register API: POST /api/auth/register</li>
                        <li>Packaged frontend: /</li>
                    </ul>
                </body>
                </html>
                """);
    }
}
