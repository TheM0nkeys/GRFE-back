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
@Table(name = "especialidades")
public class Especialidades {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "divisao_id", nullable = false)
    private Divisao divisao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id", nullable = false)
    private Departamento departamento;

    @ManyToMany(mappedBy = "especialidades")
    private Set<Funcionario> funcionarios = new HashSet<>();

    @OneToMany(mappedBy = "especialidade")
    private Set<Escala> escalas = new HashSet<>();

    @OneToMany(mappedBy = "especialidade")
    private Set<Chamado> chamados = new HashSet<>();

}
