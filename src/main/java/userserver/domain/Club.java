package userserver.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "clubs")
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class Club {
    @Id private String id;
    @NonNull @NotBlank @Size(min = 5, max = 20) private String name;
    @NonNull @Positive private Integer minAgeForJoin;
    @DBRef(db = "users") private List<User> users = new ArrayList<>();
    @Indexed @NonNull private Date createDate;
}
