package com.triptune.profile.service;

import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.ProfileImageTest;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.exception.FileBadRequestException;
import com.triptune.global.properties.DefaultProfileImageProperties;
import com.triptune.global.service.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProfileImageServiceMockTest extends ProfileImageTest {

    @InjectMocks
    private ProfileImageService profileImageService;

    @Mock
    private S3Service s3Service;

    @Mock
    private ProfileImageRepository profileImageRepository;

    @Mock
    private DefaultProfileImageProperties imageProperties;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("프로필 이미지 수정")
    void updateProfileImage() throws IOException {
        // given
        byte[] content = createTestImage("jpeg");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("newFile", "newFileOriginalName.jpeg", "image/jpeg", content);

        Member member = createMember(1L, "member");
        ProfileImage profileImage = createProfileImage(1L, "savedImage", member);

        when(profileImageRepository.findByUserId(any())).thenReturn(Optional.of(profileImage));
        when(imageProperties.getS3FileKey()).thenReturn(profileImage.getS3FileKey());

        // when
        assertDoesNotThrow(() -> profileImageService.updateProfileImage("member", mockMultipartFile));

        // then
        assertThat(profileImage.getFileName()).isNotEqualTo(mockMultipartFile.getName());
        assertThat(profileImage.getOriginalName()).isEqualTo(mockMultipartFile.getOriginalFilename());
        assertThat(profileImage.getFileSize()).isEqualTo(mockMultipartFile.getSize());
        assertThat(profileImage.getFileType()).isEqualTo("jpeg");
        assertThat(profileImage.getUpdatedAt()).isNotNull();
    }


    @Test
    @DisplayName("프로필 이미지 수정 시 허용되지 않은 확장자로 예외 발생")
    void updateProfileImage_invalidExtensionException() throws IOException {
        // given
        byte[] content = createTestImage("gif");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("newFile", "newFileOriginalName.gif", "image/gif", content);

        // when
        FileBadRequestException fail = assertThrows(FileBadRequestException.class, () -> profileImageService.updateProfileImage("member", mockMultipartFile));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.INVALID_EXTENSION.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.INVALID_EXTENSION.getMessage());
    }

    @Test
    @DisplayName("프로필 이미지 수정 시 이미지 데이터 찾을 수 없어 예외 발생")
    void updateProfileImage_ProfileImageDataNotFoundException() throws IOException {
        // given
        byte[] content = createTestImage("png");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("newFile", "newFileOriginalName.png", "image/png", content);

        when(profileImageRepository.findByUserId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> profileImageService.updateProfileImage("member", mockMultipartFile));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.PROFILE_IMAGE_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.PROFILE_IMAGE_NOT_FOUND.getMessage());
    }


}
