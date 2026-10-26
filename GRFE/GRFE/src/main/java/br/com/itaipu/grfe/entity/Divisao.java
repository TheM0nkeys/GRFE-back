package br.com.itaipu.grfe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "divisao")
public class Divisao {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @OneToMany(mappedBy = "divisao")
    private Set<Departamento> departamentos = new HashSet<>();

    @OneToMany(mappedBy = "divisao")
    private Set<Especialidades> especialidades = new HashSet<>();

}
