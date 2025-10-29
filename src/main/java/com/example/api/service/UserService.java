package com.example.api.service;

import com.example.api.dto.CareerHistoryDto;
import com.example.api.dto.UserRequest;
import com.example.api.dto.UserResponse;
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
    public Long create(UserRequest userRequest) {
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
    public void update(Long userId, UserRequest userRequest) {
        validatePeriod(userRequest);
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(ErrorCatalog.Messages.INVALID_USER_ID));
        if (userRepository.existsByNameAndIdNot(userRequest.getName(), userId)) {
            FieldErrorDetail err = ErrorCatalog.fieldError(
                    ErrorCatalog.DetailCodes.DUPLICATE,
                    ErrorCatalog.Reasons.NAME_ALREADY_EXISTS,
                    "name",
                    "body",
                    Map.of("unique", true));
            throw new ConflictException(ErrorCatalog.Messages.DUPLICATE_RESOURCE, List.of(err));
        }
        userMapper.updateEntity(userEntity, userRequest);
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
    private void validatePeriod(UserRequest userRequest) {
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
}
