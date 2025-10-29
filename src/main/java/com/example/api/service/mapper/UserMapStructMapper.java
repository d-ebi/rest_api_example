package com.example.api.service.mapper;

import com.example.api.dto.CareerHistoryDto;
import com.example.api.dto.PeriodDto;
import com.example.api.dto.UserRequest;
import com.example.api.dto.UserResponse;
import com.example.api.entity.CareerHistoryEntity;
import com.example.api.entity.UserEntity;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ユーザー関連のDTO⇄エンティティ変換を行うMapStructマッパー。
 * 日付やheightのフォーマット調整、子エンティティの逆参照設定を含みます。
 */
@Mapper(componentModel = "spring")
public interface UserMapStructMapper {
    DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * 作成用にDTOからエンティティへ変換します。
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "birthday", expression = "java(userRequest.getBirthday().format(F))")
    @Mapping(target = "height", expression = "java(roundHeight(userRequest.getHeight()))")
    @Mapping(target = "createdAt", expression = "java(now())")
    @Mapping(target = "updatedAt", expression = "java(now())")
    @Mapping(target = "careerHistories", source = "careerHistories")
    UserEntity toEntityForCreate(UserRequest userRequest);

    /**
     * 子エンティティへ親参照を付与します。
     */
    @AfterMapping
    default void backReference(@MappingTarget UserEntity userEntity) {
        if (userEntity.getCareerHistories() != null) {
            for (CareerHistoryEntity careerHistoryEntity : userEntity.getCareerHistories()) {
                careerHistoryEntity.setUser(userEntity);
            }
        }
    }

    /**
     * 更新用にDTOの内容をエンティティへ反映します。
     */
    @Mapping(target = "birthday", expression = "java(userRequest.getBirthday().format(F))")
    @Mapping(target = "height", expression = "java(roundHeight(userRequest.getHeight()))")
    @Mapping(target = "updatedAt", expression = "java(now())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "careerHistories", source = "careerHistories")
    void updateEntity(@MappingTarget UserEntity userEntity, UserRequest userRequest);

    /**
     * 更新時に職歴の差し替え・逆参照を補正します。
     */
    @AfterMapping
    default void resetCareers(@MappingTarget UserEntity userEntity, UserRequest userRequest) {
        if (userRequest.getCareerHistories() == null) {
            userEntity.setCareerHistories(null);
        } else {
            // ensure back-reference
            if (userEntity.getCareerHistories() != null) {
                for (CareerHistoryEntity careerHistoryEntity : userEntity.getCareerHistories()) {
                    careerHistoryEntity.setUser(userEntity);
                }
            }
        }
    }

    /**
     * エンティティからレスポンスDTOへ変換します。
     */
    @Mapping(target = "name", source = "name")
    @Mapping(target = "age", source = "age")
    @Mapping(target = "birthday", expression = "java(java.time.LocalDate.parse(userEntity.getBirthday(), F))")
    @Mapping(target = "height", expression = "java(userEntity.getHeight() == null ? null : new java.math.BigDecimal(String.valueOf(userEntity.getHeight())))")
    @Mapping(target = "zipCode", source = "zipCode")
    @Mapping(target = "careerHistories", source = "careerHistories")
    UserResponse toResponse(UserEntity userEntity);

    /**
     * 職歴エンティティをDTOへ変換します。
     */
    @Mapping(target = "title", source = "title")
    @Mapping(target = "period", source = ".")
    CareerHistoryDto toDto(CareerHistoryEntity careerHistoryEntity);

    /**
     * 期間情報を組み立てます。
     */
    default PeriodDto toPeriodDto(CareerHistoryEntity careerHistoryEntity) {
        PeriodDto periodDto = new PeriodDto();
        periodDto.setFrom(LocalDate.parse(careerHistoryEntity.getPeriodFrom(), F));
        periodDto.setTo(LocalDate.parse(careerHistoryEntity.getPeriodTo(), F));
        return periodDto;
    }

    /**
     * 職歴DTOをエンティティへ変換します（親参照は後段で付与）。
     */
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "title", source = "title")
    @Mapping(target = "periodFrom", expression = "java(careerHistoryDto.getPeriod().getFrom().format(F))")
    @Mapping(target = "periodTo", expression = "java(careerHistoryDto.getPeriod().getTo().format(F))")
    CareerHistoryEntity toEntity(CareerHistoryDto careerHistoryDto);

    /** DTOリスト→エンティティリスト */
    List<CareerHistoryEntity> toEntityCareers(List<CareerHistoryDto> careerHistoryDtoList);
    /** エンティティリスト→DTOリスト */
    List<CareerHistoryDto> toDtoCareers(List<CareerHistoryEntity> careerHistoryEntityList);

    /**
     * 身長を小数第1位に丸めてDoubleへ変換します。
     */
    default Double roundHeight(BigDecimal height) {
        if (height == null) return null;
        return height.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /** 現在時刻を文字列(yyyy-MM-dd HH:mm:ss)で返します。 */
    default String now() {
        return java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(java.time.LocalDateTime.now());
    }
}
