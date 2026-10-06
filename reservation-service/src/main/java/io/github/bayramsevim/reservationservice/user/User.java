package io.github.bayramsevim.reservationservice.user;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected User(){

    }

    public User(String email, String passwordHash) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email boş bırakılamaz");

        this.email = email;
        this.passwordHash = passwordHash;
        this.role = Role.USER;
    }
}
