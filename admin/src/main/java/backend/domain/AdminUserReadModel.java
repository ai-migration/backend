package backend.domain;

import javax.persistence.*;

import lombok.Data;

@Entity
@Table(name = "AdminUserReadModel_table")
@Data
public class AdminUserReadModel {
    
    @Id
    //@GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;
    private String password;
    private String nickname;
    private String role;
    
    @Column(nullable = false)
    private boolean tokenIssued;
}
