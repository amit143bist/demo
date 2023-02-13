package codinpad.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.vladmihalcea.hibernate.type.array.StringArrayType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "planet")
@TypeDefs({
        @TypeDef(name = "string-array", typeClass = StringArrayType.class)
})
public class Planet implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6789776495158587511L;

    @Id
    @Column(name = "planetid")
    String planetId;

    @Column(name = "name")
    String name;

    @Column(name = "population")
    Long population;

    @Type(type = "string-array")
    @Column(name = "terrains", columnDefinition = "text[]")
    private String[] terrains;

    @Type(type = "string-array")
    @Column(name = "climates", columnDefinition = "text[]")
    private String[] climates;
}