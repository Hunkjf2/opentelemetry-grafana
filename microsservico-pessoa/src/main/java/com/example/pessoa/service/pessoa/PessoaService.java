package com.example.pessoa.service.pessoa;

import com.example.pessoa.config.exception.CpfJaCadastradoException;
import com.example.pessoa.dto.PessoaDto;
import com.example.pessoa.config.exception.PessoaNaoEncontradaException;
import com.example.pessoa.mapper.PessoaMapper;
import com.example.pessoa.model.Pessoa;
import com.example.pessoa.repository.PessoaRepository;
import com.example.pessoa.service.log.LogService;
import com.example.pessoa.service.metrics.MetricsService;
import com.example.pessoa.service.serasa.SerasaService;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.example.pessoa.constants.global.MenssagemSistema.*;
import static com.example.pessoa.constants.log.Operacao.*;
import static com.example.pessoa.constants.pessoa.Pessoa.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final PessoaMapper pessoaMapper;
    private final LogService logService;
    private final SerasaService serasaService;
    private final MetricsService metricsService;

    public Pessoa consultar(Long id) {
        log.info("[ PESSOA ] Consultando pessoa | id={}", id);
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[ PESSOA ] Pessoa nao encontrada | id={}", id);
                    return new PessoaNaoEncontradaException(REGISTRO_NAO_ENCONTRADO);
                });
        log.info("[ PESSOA ] Pessoa encontrada | id={} | nome='{}'", pessoa.getId(), pessoa.getNome());
        return pessoa;
    }

    @Transactional
    public Pessoa cadastrar(PessoaDto pessoaDto) {
        log.info("[ PESSOA ] Iniciando cadastro | nome='{}'", pessoaDto.nome());
        Timer.Sample timer = metricsService.iniciarTimer();
        String resultado = "sucesso";
        try {
//            validarCpfUnico(pessoaDto.cpf());
            Boolean negativado = serasaService.consultarSituacaoFinanceira(pessoaDto.cpf());
            Pessoa pessoa = pessoaMapper.toEntity(pessoaDto);
            pessoa.setNegativado(negativado);
            Pessoa pessoaSalva = salvar(pessoa);
            logService.enviarDadosLog(pessoaSalva, CADASTRO);
            return pessoaSalva;
        } catch (Exception e) {
            resultado = "erro";
            log.error("[ PESSOA ] Falha no cadastro | nome='{}' | causa='{}'", pessoaDto.nome(), e.getMessage());
            throw e;
        } finally {
            metricsService.registrarCadastro(resultado);
            metricsService.registrarDuracao(timer, "cadastrar", resultado);
        }
    }

    @Transactional
    public Pessoa editar(Long id, PessoaDto pessoaDto) {
        log.info("[ PESSOA ] Iniciando edicao | id={} | nome='{}'", id, pessoaDto.nome());
        Timer.Sample timer = metricsService.iniciarTimer();
        String resultado = "sucesso";
        try {
            validarCpfUnicoParaEdicao(pessoaDto.cpf(), id);
            Pessoa pessoaAtualizada = this.salvar(atualizaDados(pessoaDto, this.consultar(id)));
            logService.enviarDadosLog(pessoaAtualizada, ATUALIZACAO);
            return pessoaAtualizada;
        } catch (Exception e) {
            resultado = "erro";
            log.error("[ PESSOA ] Falha na edicao | id={} | causa='{}'", id, e.getMessage());
            throw e;
        } finally {
            metricsService.registrarEdicao(resultado);
            metricsService.registrarDuracao(timer, "editar", resultado);
        }
    }

    @Transactional
    public void deletarPessoa(Long id) {
        Timer.Sample timer = metricsService.iniciarTimer();
        String resultado = "sucesso";
        pessoaRepository.deletarPorId(id);
        try {

            Pessoa pessoa = this.consultar(id);
            pessoaRepository.deleteById(id);
            logService.enviarDadosLog(pessoa, EXCLUSAO);
        } catch (Exception e) {
            resultado = "erro";
            log.error("[ PESSOA ] Falha na exclusao | id={} | causa='{}'", id, e.getMessage());
            throw e;
        } finally {
            metricsService.registrarExclusao(resultado);
            metricsService.registrarDuracao(timer, "deletar", resultado);
        }
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