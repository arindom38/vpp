package com.challenge.vpp.model;

import com.challenge.vpp.constant.AppConstant;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import java.time.ZonedDateTime;

@Getter
@Setter
@MappedSuperclass
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @JsonFormat(pattern = AppConstant.DATE_TIME_PATTERN, timezone = AppConstant.UTC_TIME_ZONE)
    protected ZonedDateTime createdAt;

    @JsonFormat(pattern = AppConstant.DATE_TIME_PATTERN, timezone = AppConstant.UTC_TIME_ZONE)
    protected ZonedDateTime modifiedAt;
}
