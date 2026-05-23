package com.example.pessoa.service.pessoa;

import com.example.pessoa.config.exception.CpfJaCadastradoException;
import com.example.pessoa.config.metrics.Loggable;
import com.example.pessoa.config.metrics.Metrica;
import com.example.pessoa.dto.PessoaDto;
import com.example.pessoa.config.exception.PessoaNaoEncontradaException;
import com.example.pessoa.mapper.PessoaMapper;
import com.example.pessoa.model.Pessoa;
import com.example.pessoa.repository.PessoaRepository;
import com.example.pessoa.service.log.LogService;
import com.example.pessoa.service.serasa.SerasaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.example.pessoa.constants.global.MenssagemSistema.*;
import static com.example.pessoa.constants.log.Operacao.*;
import static com.example.pessoa.constants.pessoa.Pessoa.*;

@Service
@RequiredArgsConstructor
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final PessoaMapper pessoaMapper;
    private final LogService logService;
    private final SerasaService serasaService;

    public Pessoa consultar(Long id) {
        return pessoaRepository.findById(id)
                .orElseThrow(() -> new PessoaNaoEncontradaException(REGISTRO_NAO_ENCONTRADO));
    }

    @Transactional
    @Loggable(operacao = "CADASTRO_PESSOA")
    @Metrica(
            nome = "pessoa.cadastros",
            descricao = "Cadastros de pessoa",
            operacao = "cadastrar"
    )
    public Pessoa cadastrar(PessoaDto pessoaDto) {
        Boolean negativado = serasaService.consultarSituacaoFinanceira(pessoaDto.cpf());
        Pessoa pessoa = pessoaMapper.toEntity(pessoaDto);
        pessoa.setNegativado(negativado);
        Pessoa pessoaSalva = salvar(pessoa);
        logService.enviarDadosLog(pessoaSalva, CADASTRO);
        return pessoaSalva;
    }

    @Transactional
    @Metrica(
            nome = "pessoa.edicoes",
            descricao = "Edições de pessoa",
            operacao = "editar"
    )
    public Pessoa editar(Long id, PessoaDto pessoaDto) {
        validarCpfUnicoParaEdicao(pessoaDto.cpf(), id);
        Pessoa pessoaAtualizada = salvar(atualizaDados(pessoaDto, consultar(id)));
        logService.enviarDadosLog(pessoaAtualizada, ATUALIZACAO);
        return pessoaAtualizada;
    }

    @Transactional
    @Metrica(
            nome = "pessoa.exclusoes",
            descricao = "Exclusões de pessoa",
            operacao = "deletar"
    )
    public void deletarPessoa(Long id) {
        Pessoa pessoa = consultar(id);
        pessoaRepository.deleteById(id);
        logService.enviarDadosLog(pessoa, EXCLUSAO);
    }

    private Pessoa atualizaDados(PessoaDto pessoaDto, Pessoa pessoa) {
        pessoa.setNome(pessoaDto.nome());
        pessoa.setDataNascimento(pessoaDto.dataNascimento());
        return pessoa;
    }

    private Pessoa salvar(Pessoa pessoa) {
        return pessoaRepository.save(pessoa);
    }

    private void validarCpfUnicoParaEdicao(String cpf, Long id) {
        if (pessoaRepository.existsByCpfAndIdNot(cpf, id)) {
            throw new CpfJaCadastradoException(CPF_CADASTRADO);
        }
    }

    private void validarCpfUnico(String cpf) {
        if (pessoaRepository.existsByCpf(cpf)) {
            throw new CpfJaCadastradoException(CPF_CADASTRADO);
        }
    }

}