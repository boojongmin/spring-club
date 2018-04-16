package userserver.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

@Document(collection = "users")
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class User {
    @Id private String id;
    @NonNull @NotBlank  private String name;
    @NonNull @NotNull @Positive private Integer age;
    @Indexed @NonNull private Date createDate;
}
