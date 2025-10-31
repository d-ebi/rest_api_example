package com.example.api.service;

import com.example.api.dto.*;
import com.example.api.entity.CareerHistoryEntity;
import com.example.api.entity.UserEntity;
import com.example.api.exception.*;
import com.example.api.repository.UserJpaRepository;
import com.example.api.service.mapper.UserMapStructMapper;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Epic("ユーザー管理ドメインとして")
@ExtendWith({MockitoExtension.class, AllureJunit5.class})
@DisplayName("UserServiceのユースケース検証")
class UserServiceTest {

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private UserMapStructMapper userMapper;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userMapper);
    }

    private UserCreateRequest createRequest(String name) {
        UserCreateRequest request = new UserCreateRequest();
        request.setName(name);
        request.setAge(30);
        request.setBirthday(LocalDate.of(1990, 1, 1));
        request.setHeight(new BigDecimal("170.4"));
        request.setZipCode("123-4567");
        return request;
    }

    private CareerHistoryDto careerHistoryDto(String title, LocalDate from, LocalDate to) {
        CareerHistoryDto dto = new CareerHistoryDto();
        dto.setTitle(title);
        PeriodDto period = new PeriodDto();
        period.setFrom(from);
        period.setTo(to);
        dto.setPeriod(period);
        return dto;
    }

    private CareerHistoryUpdateDto careerHistoryUpdateDto(Long id, String title, LocalDate from, LocalDate to) {
        CareerHistoryUpdateDto dto = new CareerHistoryUpdateDto();
        dto.setId(id);
        dto.setTitle(title);
        PeriodUpdateDto period = new PeriodUpdateDto();
        period.setFrom(from);
        period.setTo(to);
        dto.setPeriod(period);
        return dto;
    }

    @Nested
    @Feature("ユーザー作成機能を利用する場合")
    @DisplayName("createの振る舞い")
    class CreateTests {

        @Test
        @Story("入力が正しく一意なユーザーを登録する")
        @DisplayName("一意な名前ならユーザーIDを返す")
        @Tag("種別:正常系")
        @Tag("同値分類:正常値")
        void returnGeneratedIdWhenNameIsUnique() {
            UserCreateRequest request = createRequest("山田太郎");
            request.setCareerHistories(List.of(careerHistoryDto(
                    "エンジニア",
                    LocalDate.of(2010, 4, 1),
                    LocalDate.of(2012, 3, 31)
            )));

            CareerHistoryEntity historyEntity = CareerHistoryEntity.builder()
                    .title("エンジニア")
                    .periodFrom("2010/04/01")
                    .periodTo("2012/03/31")
                    .build();
            UserEntity mappedEntity = UserEntity.builder()
                    .name(request.getName())
                    .age(request.getAge())
                    .birthday(request.getBirthday().format(UserMapStructMapper.F))
                    .height(170.4)
                    .zipCode(request.getZipCode())
                    .careerHistories(new ArrayList<>(List.of(historyEntity)))
                    .build();

            when(userRepository.existsByName("山田太郎")).thenReturn(false);
            when(userMapper.toEntityForCreate(request)).thenReturn(mappedEntity);
            when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
                UserEntity entity = invocation.getArgument(0);
                entity.setId(10L);
                return entity;
            });

            Long actual = userService.create(request);

            assertEquals(10L, actual);
            ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(captor.capture());
            UserEntity saved = captor.getValue();
            assertEquals(1, saved.getCareerHistories().size());
            assertSame(saved, saved.getCareerHistories().get(0).getUser());
        }

        @Test
        @Story("入力の名前が既に存在している")
        @DisplayName("重複する名前なら競合エラーを送出する")
        @Tag("種別:異常系")
        @Tag("観点:一意制約")
        void throwConflictWhenNameExists() {
            UserCreateRequest request = createRequest("山田太郎");
            when(userRepository.existsByName("山田太郎")).thenReturn(true);

            assertThrows(ConflictException.class, () -> userService.create(request));

            verify(userRepository, never()).save(any());
            verify(userMapper, never()).toEntityForCreate(any());
        }

        @Test
        @Story("職歴期間の開始と終了が逆転している")
        @DisplayName("期間が逆転している職歴が含まれる場合は検証エラーを送出する")
        @Tag("種別:異常系")
        @Tag("観点:整合性チェック")
        void throwUnprocessableWhenPeriodInvalid() {
            UserCreateRequest request = createRequest("佐藤花子");
            request.setCareerHistories(List.of(careerHistoryDto(
                    "エンジニア",
                    LocalDate.of(2020, 2, 1),
                    LocalDate.of(2020, 1, 1)
            )));

            assertThrows(UnprocessableEntityException.class, () -> userService.create(request));

            verifyNoInteractions(userRepository, userMapper);
        }
    }

    @Nested
    @Feature("ユーザー更新機能を操作する場合")
    @DisplayName("updateの振る舞い")
    class UpdateTests {

        @Test
        @Story("既存ユーザーの属性を正常に更新する")
        @DisplayName("更新対象が存在し重複がない場合は必要な項目が更新される")
        @Tag("種別:正常系")
        @Tag("同値分類:正常値")
        void updateEntityWhenInputsValid() {
            Long userId = 1L;
            CareerHistoryEntity existingHistory = CareerHistoryEntity.builder()
                    .id(5L)
                    .title("旧職歴")
                    .periodFrom("2010/04/01")
                    .periodTo("2015/03/31")
                    .build();
            UserEntity existing = UserEntity.builder()
                    .id(userId)
                    .name("旧名前")
                    .age(25)
                    .birthday("1988/01/01")
                    .height(168.0)
                    .zipCode("000-0000")
                    .updatedAt("2020-01-01 00:00:00")
                    .careerHistories(new ArrayList<>(List.of(existingHistory)))
                    .build();
            existingHistory.setUser(existing);

            UserUpdateRequest request = new UserUpdateRequest();
            request.setName("新しい名前");
            request.setAge(35);
            request.setBirthday(LocalDate.of(1989, 5, 20));
            request.setHeight(new BigDecimal("170.16"));
            request.setZipCode("234-5678");
            request.setCareerHistories(List.of(
                    careerHistoryUpdateDto(5L, "改訂職歴", LocalDate.of(2010, 4, 1), LocalDate.of(2016, 3, 31))
            ));

            when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
            when(userRepository.existsByNameAndIdNot("新しい名前", userId)).thenReturn(false);
            when(userMapper.now()).thenReturn("2024-06-01 12:34:56");

            userService.update(userId, request);

            assertEquals("新しい名前", existing.getName());
            assertEquals(35, existing.getAge());
            assertEquals("1989/05/20", existing.getBirthday());
            assertEquals(170.2, existing.getHeight());
            assertEquals("234-5678", existing.getZipCode());
            assertEquals("2024-06-01 12:34:56", existing.getUpdatedAt());
            CareerHistoryEntity updatedHistory = existing.getCareerHistories().get(0);
            assertEquals("改訂職歴", updatedHistory.getTitle());
            assertEquals("2010/04/01", updatedHistory.getPeriodFrom());
            assertEquals("2016/03/31", updatedHistory.getPeriodTo());
            assertSame(existing, updatedHistory.getUser());
            verify(userRepository).save(existing);
        }

        @Test
        @Story("指定したIDのユーザーが存在しない")
        @DisplayName("存在しないユーザーIDなら不正リクエストエラーを送出する")
        @Tag("種別:異常系")
        @Tag("観点:ID整合性")
        void throwBadRequestWhenUserMissing() {
            Long userId = 99L;
            UserUpdateRequest request = new UserUpdateRequest();

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(BadRequestException.class, () -> userService.update(userId, request));

            verify(userRepository, never()).save(any());
        }

        @Test
        @Story("更新時に同名の別ユーザーが存在する")
        @DisplayName("名前が重複している場合は競合エラーを送出する")
        @Tag("種別:異常系")
        @Tag("観点:一意制約")
        void throwConflictWhenDuplicateNameExists() {
            Long userId = 1L;
            UserUpdateRequest request = new UserUpdateRequest();
            request.setName("重複名前");
            UserEntity existing = UserEntity.builder().id(userId).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
            when(userRepository.existsByNameAndIdNot("重複名前", userId)).thenReturn(true);

            assertThrows(ConflictException.class, () -> userService.update(userId, request));

            verify(userRepository, never()).save(any());
        }

        @Test
        @Story("新規追加する職歴の必須項目が欠落している")
        @DisplayName("必須項目不足の新規職歴を指定すると加工できないエラーを送出する")
        @Tag("種別:異常系")
        @Tag("観点:入力チェック")
        void throwUnprocessableWhenNewCareerIncomplete() {
            Long userId = 1L;
            UserEntity existing = UserEntity.builder()
                    .id(userId)
                    .careerHistories(new ArrayList<>())
                    .build();
            CareerHistoryUpdateDto dto = new CareerHistoryUpdateDto();
            dto.setTitle("新しい職歴");
            UserUpdateRequest request = new UserUpdateRequest();
            request.setCareerHistories(List.of(dto));

            when(userRepository.findById(userId)).thenReturn(Optional.of(existing));

            assertThrows(UnprocessableEntityException.class, () -> userService.update(userId, request));

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @Feature("ユーザー削除機能を利用する場合")
    @DisplayName("deleteの振る舞い")
    class DeleteTests {

        @Test
        @Story("対象IDのユーザーが存在している")
        @DisplayName("存在するIDなら削除処理を呼び出す")
        @Tag("種別:正常系")
        @Tag("観点:状態遷移")
        void deleteByIdWhenUserExists() {
            when(userRepository.existsById(1L)).thenReturn(true);

            userService.delete(1L);

            verify(userRepository).deleteById(1L);
        }

        @Test
        @Story("対象IDのユーザーが存在しない")
        @DisplayName("存在しないIDなら削除処理は呼ばれない")
        @Tag("種別:正常系")
        @Tag("観点:無操作確認")
        void skipDeleteWhenUserMissing() {
            when(userRepository.existsById(1L)).thenReturn(false);

            userService.delete(1L);

            verify(userRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @Feature("ユーザー詳細取得機能を利用する場合")
    @DisplayName("getの振る舞い")
    class GetTests {

        @Test
        @Story("指定したIDのユーザーが存在する")
        @DisplayName("存在するIDならDTOを返す")
        @Tag("種別:正常系")
        @Tag("観点:同値分類:正常値")
        void returnResponseWhenUserExists() {
            UserEntity entity = UserEntity.builder().id(1L).build();
            UserResponse response = UserResponse.builder().id(1L).name("山田太郎").build();

            when(userRepository.findWithCareerHistoriesById(1L)).thenReturn(Optional.of(entity));
            when(userMapper.toResponse(entity)).thenReturn(response);

            UserResponse actual = userService.get(1L);

            assertSame(response, actual);
        }

        @Test
        @Story("指定したIDのユーザーが存在しない")
        @DisplayName("存在しないIDなら見つからないエラーを送出する")
        @Tag("種別:異常系")
        @Tag("観点:ID整合性")
        void throwNotFoundWhenUserMissing() {
            when(userRepository.findWithCareerHistoriesById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> userService.get(1L));
        }
    }

    @Nested
    @Feature("ユーザー件数取得機能を利用する場合")
    @DisplayName("countの振る舞い")
    class CountTests {

        @Test
        @Story("名前条件を指定しない")
        @DisplayName("名前が空白なら全件数を返す")
        @Tag("種別:正常系")
        @Tag("観点:同値分類:正常値")
        void returnTotalCountWhenNameBlank() {
            when(userRepository.count()).thenReturn(7L);

            int actual = userService.count("  ");

            assertEquals(7, actual);
        }

        @Test
        @Story("名前条件を指定する")
        @DisplayName("名前が指定されていれば部分一致件数を返す")
        @Tag("種別:正常系")
        @Tag("観点:同値分類:正常値")
        void returnFilteredCountWhenNameProvided() {
            when(userRepository.countByNameContaining("田")).thenReturn(3L);

            int actual = userService.count("田");

            assertEquals(3, actual);
        }
    }

    @Nested
    @Feature("ユーザー一覧取得機能を利用する場合")
    @DisplayName("listの振る舞い")
    class ListTests {

        @Test
        @Story("制限なしで一覧を取得しオフセットを適用する")
        @DisplayName("limitが0なら全件取得しオフセット分をスキップする")
        @Tag("種別:正常系")
        @Tag("観点:ページング")
        void listSkipsOffsetWhenLimitZero() {
            UserEntity first = UserEntity.builder().id(1L).name("A").build();
            UserEntity second = UserEntity.builder().id(2L).name("B").build();
            UserEntity third = UserEntity.builder().id(3L).name("C").build();

            when(userRepository.findAll(Mockito.<Specification<UserEntity>>any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(first, second, third)));
            when(userMapper.toResponse(any(UserEntity.class))).thenAnswer(invocation -> {
                UserEntity entity = invocation.getArgument(0);
                return UserResponse.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .build();
            });

            List<UserResponse> actual = userService.list(null, 0, 1);

            assertEquals(2, actual.size());
            assertEquals(2L, actual.get(0).getId());
            assertEquals(3L, actual.get(1).getId());
            verify(userMapper, times(2)).toResponse(any());
        }
    }
}
