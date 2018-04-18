package userserver.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Mono;
import userserver.repository.ClubRepository;
import userserver.repository.UserRepository;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.*;

@Document(collection = "clubs")
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class Club {
    @Id private String id;
    @NonNull @NotBlank @Size(min = 5, max = 20) private String name;
    @NonNull @Positive private Integer minAgeForJoin;
    @Indexed @NonNull private Date createDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Club club = (Club) o;
        return Objects.equals(id, club.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Club{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", minAgeForJoin=" + minAgeForJoin +
                ", createDate=" + createDate +
                '}';
    }
}
