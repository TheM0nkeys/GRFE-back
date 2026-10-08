package br.com.itaipu.grfe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "funcionario")
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String matricula;

    private String email;

    @ManyToMany
    @JoinTable(
            name = "funcionario_especialidades",
            joinColumns = @JoinColumn(name = "funcionario_id"),
            inverseJoinColumns = @JoinColumn(name = "especialidade_id")
    )
    private Set<Especialidades> especialidades = new HashSet<>();

    @OneToMany(mappedBy = "funcionario")
    private Set<Escala> escalas = new HashSet<>();

    @OneToMany(mappedBy = "plantonista")
    private Set<Chamado> chamadosComoPlantonista = new HashSet<>();

    @OneToMany(mappedBy = "usuarioResponsavel")
    private Set<Chamado> chamadosComoResponsavel = new HashSet<>();

}
