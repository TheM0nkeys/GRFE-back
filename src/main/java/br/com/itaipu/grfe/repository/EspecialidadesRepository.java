package br.com.itaipu.grfe.repository;

import br.com.itaipu.grfe.entity.Especialidades;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EspecialidadesRepository extends JpaRepository<Especialidades, Long> {

}
