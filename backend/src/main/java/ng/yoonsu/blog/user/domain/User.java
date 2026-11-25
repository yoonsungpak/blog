package ng.yoonsu.blog.user.domain;

import jakarta.persistence.*;
import jakarta.websocket.ClientEndpoint;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.VISITOR;

    @Column(name = "created_at", nullable = false)
    private final Instant createdAt = Instant.now();

    @Column(name = "deleted_at", nullable = true)
    private Instant deletedAt = null;

    protected User() {
    }

    public User(String username, String hashed) {
        this.username = username;
        this.password = hashed;
    }

    public String getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public void changePassword(String hashed) {
        this.password = hashed;
    }

    public Role getRole() {
        return this.role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getDeletedAt() {
        return this.deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
