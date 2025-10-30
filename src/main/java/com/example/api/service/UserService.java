package com.example.api.service;

import com.example.api.dto.*;
import com.example.api.entity.CareerHistoryEntity;
import com.example.api.entity.UserEntity;
import com.example.api.exception.*;
import com.example.api.repository.UserJpaRepository;
import com.example.api.repository.spec.UserSpecifications;
import com.example.api.service.mapper.UserMapStructMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ユーザーに関するアプリケーションサービス。
 * バリデーション、重複チェック、JPAリポジトリ呼び出し、DTOマッピングを担います。
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserJpaRepository userRepository;
    private final UserMapStructMapper userMapper;

    /**
     * ユーザーを新規作成します。
     * @param userRequest 作成内容
     * @return 生成されたユーザーID
     * @throws ConflictException nameの一意制約違反
     * @throws UnprocessableEntityException 期間の整合性エラー
     */
    @Transactional
    public Long create(UserCreateRequest userRequest) {
        validatePeriod(userRequest);
        if (userRepository.existsByName(userRequest.getName())) {
            FieldErrorDetail err = ErrorCatalog.fieldError(
                    ErrorCatalog.DetailCodes.DUPLICATE,
                    ErrorCatalog.Reasons.NAME_ALREADY_EXISTS,
                    "name",
                    "body",
                    Map.of("unique", true));
            throw new ConflictException(ErrorCatalog.Messages.DUPLICATE_RESOURCE, List.of(err));
        }
        UserEntity userEntity = userMapper.toEntityForCreate(userRequest);
        if (userEntity.getCareerHistories() != null) {
            userEntity.getCareerHistories().forEach(ch -> ch.setUser(userEntity));
        }
        UserEntity savedUser = userRepository.save(userEntity);
        return savedUser.getId();
    }

    /**
     * ユーザーを更新します。
     * @param userId 対象ユーザーID
     * @param userRequest 更新内容
     * @throws BadRequestException IDが不正
     * @throws ConflictException nameの一意制約違反
     * @throws UnprocessableEntityException 期間の整合性エラー
     */
    @Transactional
    public void update(Long userId, UserUpdateRequest userRequest) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(ErrorCatalog.Messages.INVALID_USER_ID));

        if (userRequest.getName() != null) {
            if (userRepository.existsByNameAndIdNot(userRequest.getName(), userId)) {
                FieldErrorDetail err = ErrorCatalog.fieldError(
                        ErrorCatalog.DetailCodes.DUPLICATE,
                        ErrorCatalog.Reasons.NAME_ALREADY_EXISTS,
                        "name",
                        "body",
                        Map.of("unique", true));
                throw new ConflictException(ErrorCatalog.Messages.DUPLICATE_RESOURCE, List.of(err));
            }
            userEntity.setName(userRequest.getName());
        }
        if (userRequest.getAge() != null) {
            userEntity.setAge(userRequest.getAge());
        }
        if (userRequest.getBirthday() != null) {
            userEntity.setBirthday(userRequest.getBirthday().format(UserMapStructMapper.F));
        }
        if (userRequest.getHeight() != null) {
            userEntity.setHeight(roundHeight(userRequest.getHeight()));
        }
        if (userRequest.getZipCode() != null) {
            userEntity.setZipCode(userRequest.getZipCode());
        }

        if (userRequest.getCareerHistories() != null) {
            updateCareerHistories(userEntity, userRequest.getCareerHistories());
        }

        validatePeriod(userEntity, userRequest);
        userEntity.setUpdatedAt(now());
        userRepository.save(userEntity);
    }

    /**
     * ユーザーを削除します（存在しないIDでもエラーとしません）。
     * @param userId 対象ユーザーID
     */
    @Transactional
    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }

    /**
     * ユーザーをIDで取得します。
     * @param userId 対象ユーザーID
     * @return ユーザーDTO
     * @throws ResourceNotFoundException 見つからない場合
     */
    @Transactional(readOnly = true)
    public UserResponse get(Long userId) {
        UserEntity userEntity = userRepository.findWithCareerHistoriesById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCatalog.Messages.USER_NOT_FOUND));
        return userMapper.toResponse(userEntity);
    }

    /**
     * 条件に合致する総件数を返します。
     * @param name 名前の部分一致（任意）
     * @return 総件数
     */
    @Transactional(readOnly = true)
    public int count(String name) {
        if (name == null || name.isBlank()) return (int) userRepository.count();
        return (int) userRepository.countByNameContaining(name);
    }

    /**
     * ユーザー一覧を返します。
     * @param name 名前の部分一致（任意）
     * @param limit 取得件数（0は全件相当でoffset以降）
     * @param offset 取得開始位置
     * @return ユーザーDTOのリスト
     */
    @Transactional(readOnly = true)
    public List<UserResponse> list(String name, int limit, int offset) {
        int pageNumber = (limit == 0) ? 0 : offset / Math.max(1, limit);
        Pageable pageable = (limit == 0) ? PageRequest.of(0, Integer.MAX_VALUE) : PageRequest.of(pageNumber, limit);
        Page<UserEntity> resultPage = userRepository.findAll(UserSpecifications.nameContains(name), pageable);
        List<UserEntity> userEntities = resultPage.getContent();
        if (limit == 0 && offset > 0 && offset < userEntities.size()) {
            userEntities = userEntities.subList(offset, userEntities.size());
        }
        return userEntities.stream().map(userMapper::toResponse).collect(Collectors.toList());
    }

    /**
     * 職歴の期間整合性を検証します（from <= to）。
     * @param userRequest 対象リクエスト
     * @throws UnprocessableEntityException 期間の整合性エラー
     */
    private void validatePeriod(UserCreateRequest userRequest) {
        if (userRequest.getCareerHistories() != null) {
            for (CareerHistoryDto careerHistory : userRequest.getCareerHistories()) {
                if (careerHistory.getPeriod() == null || careerHistory.getPeriod().getFrom() == null || careerHistory.getPeriod().getTo() == null) continue;
                if (careerHistory.getPeriod().getFrom().isAfter(careerHistory.getPeriod().getTo())) {
                    FieldErrorDetail err = ErrorCatalog.fieldError(
                            ErrorCatalog.DetailCodes.INVALID_PERIOD,
                            ErrorCatalog.Reasons.PERIOD_FROM_AFTER_TO,
                            "careerHistories.period",
                            "body",
                            Map.of("from", careerHistory.getPeriod().getFrom().toString(),
                                    "to", careerHistory.getPeriod().getTo().toString()));
                    throw new UnprocessableEntityException(ErrorCatalog.Messages.INVALID_PERIOD, List.of(err));
                }
            }
        }
    }

    /**
     * 更新リクエストの期間整合性を検証します。
     *
     * @param userEntity 現在のユーザーエンティティ
     * @param userRequest 更新内容
     */
    private void validatePeriod(UserEntity userEntity, UserUpdateRequest userRequest) {
        if (userRequest.getCareerHistories() == null) return;
        Map<Long, CareerHistoryEntity> existing = userEntity.getCareerHistories() == null
                ? Collections.emptyMap()
                : userEntity.getCareerHistories().stream().filter(e -> e.getId() != null)
                .collect(Collectors.toMap(CareerHistoryEntity::getId, e -> e));

        for (CareerHistoryUpdateDto dto : userRequest.getCareerHistories()) {
            PeriodUpdateDto period = dto.getPeriod();
            if (period == null && dto.getId() == null) continue;

            LocalDate currentFrom = null;
            LocalDate currentTo = null;
            if (dto.getId() != null && existing.containsKey(dto.getId())) {
                CareerHistoryEntity entity = existing.get(dto.getId());
                currentFrom = parseDate(entity.getPeriodFrom());
                currentTo = parseDate(entity.getPeriodTo());
            }
            LocalDate newFrom = period != null && period.getFrom() != null ? period.getFrom() : currentFrom;
            LocalDate newTo = period != null && period.getTo() != null ? period.getTo() : currentTo;

            if (newFrom != null && newTo != null && newFrom.isAfter(newTo)) {
                FieldErrorDetail err = ErrorCatalog.fieldError(
                        ErrorCatalog.DetailCodes.INVALID_PERIOD,
                        ErrorCatalog.Reasons.PERIOD_FROM_AFTER_TO,
                        "careerHistories.period",
                        "body",
                        Map.of("from", newFrom.toString(), "to", newTo.toString()));
                throw new UnprocessableEntityException(ErrorCatalog.Messages.INVALID_PERIOD, List.of(err));
            }
        }
    }

    /**
     * 更新DTOの内容で職歴エンティティ一覧を差分更新します。
     *
     * @param userEntity 対象ユーザー
     * @param updates    更新リクエスト
     */
    private void updateCareerHistories(UserEntity userEntity, List<CareerHistoryUpdateDto> updates) {
        List<CareerHistoryEntity> current = userEntity.getCareerHistories() == null
                ? new ArrayList<>()
                : new ArrayList<>(userEntity.getCareerHistories());

        Map<Long, CareerHistoryEntity> existing = current.stream()
                .filter(entity -> entity.getId() != null)
                .collect(Collectors.toMap(CareerHistoryEntity::getId, entity -> entity));

        for (CareerHistoryUpdateDto dto : updates) {
            CareerHistoryEntity entity = null;
            if (dto.getId() != null) {
                entity = existing.get(dto.getId());
            }
            if (entity == null) {
                entity = new CareerHistoryEntity();
                entity.setUser(userEntity);
                current.add(entity);
            }

            if (dto.getTitle() != null) {
                entity.setTitle(dto.getTitle());
            }

            PeriodUpdateDto period = dto.getPeriod();
            if (period != null) {
                if (period.getFrom() != null) {
                    entity.setPeriodFrom(period.getFrom().format(UserMapStructMapper.F));
                }
                if (period.getTo() != null) {
                    entity.setPeriodTo(period.getTo().format(UserMapStructMapper.F));
                }
            }

            entity.setUser(userEntity);

            if (dto.getId() == null) {
                if (entity.getTitle() == null || entity.getPeriodFrom() == null || entity.getPeriodTo() == null) {
                    FieldErrorDetail err = ErrorCatalog.fieldError(
                            ErrorCatalog.DetailCodes.VALIDATION_ERROR,
                            ErrorCatalog.Messages.VALIDATION_FAILED,
                            "careerHistories",
                            "body",
                            Map.of("message", "title and period are required for new entries"));
                    throw new UnprocessableEntityException(ErrorCatalog.Messages.UNPROCESSABLE_TOP, List.of(err));
                }
            }
        }

        userEntity.setCareerHistories(current);
    }

    /**
     * 身長を小数第1位で四捨五入しDoubleへ変換します。
     */
    private Double roundHeight(BigDecimal height) {
        if (height == null) return null;
        return height.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * yyyy/MM/dd 文字列をLocalDateへ変換します。
     */
    private LocalDate parseDate(String value) {
        if (value == null) return null;
        return LocalDate.parse(value, UserMapStructMapper.F);
    }

    /**
     * マッパーの現在時刻フォーマッタを利用して現在時刻文字列を取得します。
     */
    private String now() {
        return userMapper.now();
    }
}
