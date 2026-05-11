package com.legallyshop.legallyshop.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_address")
@Data
@NoArgsConstructor
public class UserAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 100)
    private String province;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String ward;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "is_default")
    private Boolean isDefault = false;
}
