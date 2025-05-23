package com.triptune.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FindIdRequest {

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    private String email;

    @Builder
    public FindIdRequest(String email) {
        this.email = email;
    }
}
