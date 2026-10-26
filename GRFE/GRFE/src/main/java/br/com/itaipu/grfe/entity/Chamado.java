package br.com.itaipu.grfe.entity;

import br.com.itaipu.grfe.entity.enums.StatusChamado;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chamado")
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataHoraAcionamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidade_id", nullable = false)
    private Especialidades especialidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plantonista_id", nullable = false)
    private Funcionario plantonista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_responsavel_id", nullable = false)
    private Funcionario usuarioResponsavel;

    @Column(nullable = false)
    private String motivo;

    private String numeroIncidente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusChamado status;

    @OneToMany(mappedBy = "chamado")
    private Set<HistoricoAcionamento> historico = new HashSet<>();

}
