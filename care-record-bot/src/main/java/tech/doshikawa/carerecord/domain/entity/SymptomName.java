package tech.doshikawa.carerecord.domain.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 症状名マスタを表すEntity
 */
@Data
@Builder
@Table(name = "symptom_name", schema = "core")
public class SymptomName {
    @Id
    private Integer id;
    private Integer symptomCategoryId;
    private String symptomName;
}
