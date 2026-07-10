package tech.doshikawa.carerecord.application.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * JWTを用いた「使い捨ての通行符」の発行と検証を司る暗号局
 */
@Slf4j
@Service
public class JwtTokenService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtTokenService(@Value("${line.bot.channel-secret}") String secret) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(this.algorithm)
                .withIssuer("care-record-bot")
                .build();
    }

    /**
     * 指定されたユーザーIDのCSVダウンロード専用の密書（トークン）を発行する
     * @param userId 対象のユーザーID
     * @return 10分間だけ有効な暗号化されたJWTトークン
     */
    public String generateCsvDownloadToken(String userId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(10, ChronoUnit.MINUTES);

        return JWT.create()
                .withIssuer("care-record-bot")
                .withSubject(userId)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(algorithm);
    }

    /**
     * 提出された密書（トークン）の正当性を検証し、ユーザーIDを抽出する
     * @param token ユーザーが提示したトークン
     * @return 検証に成功した場合はユーザーID。失敗（改ざん・期限切れ）の場合は null
     */
    public String verifyAndExtractUserId(String token) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException e) {
            log.warn("無効な通行符を検知しました: {}", e.getMessage());
            return null;
        }
    }
}
