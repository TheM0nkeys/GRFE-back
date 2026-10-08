package br.com.itaipu.grfe.specification;

import br.com.itaipu.grfe.entity.Chamado;
import br.com.itaipu.grfe.entity.enums.StatusChamado;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ChamadoSpecification {

    private ChamadoSpecification() {
    }

    public static Specification<Chamado> comFiltros(StatusChamado status,
                                                    Long especialidadeId,
                                                    Long plantonistaId,
                                                    Long divisaoId,
                                                    Long departamentoId,
                                                    LocalDateTime dataInicio,
                                                    LocalDateTime dataFim) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (especialidadeId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("especialidade").get("id"), especialidadeId));
            }
            if (plantonistaId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("plantonista").get("id"), plantonistaId));
            }
            if (divisaoId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("especialidade").get("divisao").get("id"), divisaoId));
            }
            if (departamentoId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("especialidade").get("departamento").get("id"), departamentoId));
            }
            if (dataInicio != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("dataHoraAcionamento"), dataInicio));
            }
            if (dataFim != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("dataHoraAcionamento"), dataFim));
            }

            return predicate;
        };
    }
}