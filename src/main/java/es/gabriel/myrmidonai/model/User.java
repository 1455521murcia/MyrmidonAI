package es.gabriel.myrmidonai.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //para que no sea secuencia
    private Long id;

    @Column(unique = true,nullable = false)
    private String username;

    private String password;
    @Enumerated(EnumType.STRING) // se usa por si se pudiese cambiar el nombre de algún rol que lo guarde como string
    private Role role;
}
