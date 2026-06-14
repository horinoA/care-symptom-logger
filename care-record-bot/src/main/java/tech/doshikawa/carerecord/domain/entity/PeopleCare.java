package tech.doshikawa.carerecord.domain.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 登場人物（Who/To Who）マスタを表すEntity
 */
@Data
@Builder
@Table(name = "people_care", schema = "core")
public class PeopleCare {
    @Id
    private Integer id;
    private String relationship;
    private Integer sortOrder;
    private Boolean isVisible;
}
