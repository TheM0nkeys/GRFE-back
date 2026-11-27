package br.com.itaipu.grfe.repository;

import br.com.itaipu.grfe.entity.Usuario;
import br.com.itaipu.grfe.entity.enums.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = "funcionario")
    Optional<Usuario> findByLoginIgnoreCase(String login);

    @Override
    @EntityGraph(attributePaths = "funcionario")
    List<Usuario> findAll();

    boolean existsByLoginIgnoreCase(String login);

    boolean existsByFuncionarioId(Long funcionarioId);

    long countByRole(Role role);

}
