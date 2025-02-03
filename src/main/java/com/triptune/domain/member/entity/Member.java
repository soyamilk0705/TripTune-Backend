package com.triptune.domain.member.entity;

import com.triptune.domain.bookmark.entity.Bookmark;
import com.triptune.domain.member.dto.request.JoinRequest;
import com.triptune.domain.profile.entity.ProfileImage;
import com.triptune.domain.schedule.entity.TravelAttendee;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProfileImage profileImage;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "password")
    private String password;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "is_social_login")
    private boolean isSocialLogin;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "email")
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TravelAttendee> travelAttendeeList = new ArrayList<>();

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Bookmark> bookmarkList = new ArrayList<>();

    @Builder
    public Member(Long memberId, ProfileImage profileImage, String userId, String password, String refreshToken, boolean isSocialLogin, String nickname, String email, LocalDateTime createdAt, LocalDateTime updatedAt, List<TravelAttendee> travelAttendeeList, List<Bookmark> bookmarkList) {
        this.memberId = memberId;
        this.profileImage = profileImage;
        this.userId = userId;
        this.password = password;
        this.refreshToken = refreshToken;
        this.isSocialLogin = isSocialLogin;
        this.nickname = nickname;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.travelAttendeeList = travelAttendeeList;
        this.bookmarkList = bookmarkList;
    }

    public static Member from(JoinRequest joinRequest, String encodePassword){
        return Member.builder()
                .userId(joinRequest.getUserId())
                .password(encodePassword)
                .nickname(joinRequest.getNickname())
                .email(joinRequest.getEmail())
                .isSocialLogin(false)
                .createdAt(LocalDateTime.now())
                .build();
    }


    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updatePassword(String password) {
        this.password = password;
        updateUpdatedAt();
    }

    public boolean isMatchRefreshToken(String refreshToken){
        return this.refreshToken.equals(refreshToken);
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
        updateUpdatedAt();
    }

    public void updateUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

}
