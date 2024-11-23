package com.triptune.domain.member.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RefreshTokenResponse {

    private String accessToken;

    @Builder
    public RefreshTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }


    public static RefreshTokenResponse of(String accessToken){
        return new RefreshTokenResponse(accessToken);
    }
}