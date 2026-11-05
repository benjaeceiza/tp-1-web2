package ar.edu.unvime.apiblank.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.unvime.apiblank.domain.Favorito;
import ar.edu.unvime.apiblank.domain.Lista;
import ar.edu.unvime.apiblank.dto.lista.ListaRequestDTO;
import ar.edu.unvime.apiblank.dto.lista.ListaResponseDTO;
import ar.edu.unvime.apiblank.exception.AccesoDenegadoException;
import ar.edu.unvime.apiblank.mapper.ListaMapper;
import ar.edu.unvime.apiblank.repository.FavoritoRepository;
import ar.edu.unvime.apiblank.repository.ListaRepository;
import ar.edu.unvime.apiblank.security.ContextoSeguridad;

@ExtendWith(MockitoExtension.class)
class ListaServiceTest {

    private static final Long ANA_ID = 10L;
    private static final Long BOB_ID = 20L;

    @Mock
    private ListaRepository listaRepository;

    @Mock
    private FavoritoRepository favoritoRepository;

    @Mock
    private ContextoSeguridad contextoSeguridad;

    @Captor
    private ArgumentCaptor<Lista> listaCaptor;

    private ListaService service;

    @BeforeEach
    void setUp() {
        service = new ListaService(listaRepository, new ListaMapper(), favoritoRepository, contextoSeguridad);
    }

    @Test
    void guardar_usuarioAutenticado_asociaListaAlUsuarioAutenticado() {
        when(contextoSeguridad.usuarioActualId()).thenReturn(ANA_ID);
        when(listaRepository.save(any(Lista.class))).thenAnswer(inv -> {
            Lista l = inv.getArgument(0);
            l.setId(1L);
            return l;
        });

        ListaResponseDTO response = service.guardar(new ListaRequestDTO("Lista de compras"));

        verify(listaRepository).save(listaCaptor.capture());
        assertThat(listaCaptor.getValue().getUsuarioId()).isEqualTo(ANA_ID);
        assertThat(response.usuarioId()).isEqualTo(ANA_ID);
        assertThat(response.nombre()).isEqualTo("Lista de compras");
    }

    @Test
    void obtenerPorId_listaPropia_devuelveLaLista() {
        when(listaRepository.findById(7L)).thenReturn(Optional.of(new Lista(7L, "De Ana", ANA_ID)));
        when(contextoSeguridad.usuarioActualId()).thenReturn(ANA_ID);

        ListaResponseDTO resultado = service.obtenerPorId(7L);

        assertThat(resultado.id()).isEqualTo(7L);
        assertThat(resultado.usuarioId()).isEqualTo(ANA_ID);
    }

    @Test
    void obtenerPorId_listaAjena_lanzaAccesoDenegado() {
        when(listaRepository.findById(7L)).thenReturn(Optional.of(new Lista(7L, "De Ana", ANA_ID)));
        when(contextoSeguridad.usuarioActualId()).thenReturn(BOB_ID);
        when(contextoSeguridad.esAdmin()).thenReturn(false);

        assertThatThrownBy(() -> service.obtenerPorId(7L))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void obtenerPorId_listaAjenaComoAdmin_devuelveLaLista() {
        when(listaRepository.findById(7L)).thenReturn(Optional.of(new Lista(7L, "De Ana", ANA_ID)));
        when(contextoSeguridad.usuarioActualId()).thenReturn(BOB_ID);
        when(contextoSeguridad.esAdmin()).thenReturn(true);

        ListaResponseDTO resultado = service.obtenerPorId(7L);

        assertThat(resultado.id()).isEqualTo(7L);
        assertThat(resultado.nombre()).isEqualTo("De Ana");
    }

    @Test
    void obtenerTodas_usuarioComun_devuelveSoloSusListas() {
        when(contextoSeguridad.esAdmin()).thenReturn(false);
        when(contextoSeguridad.usuarioActualId()).thenReturn(ANA_ID);
        when(listaRepository.findByUsuarioId(ANA_ID))
                .thenReturn(List.of(new Lista(1L, "Mis libros", ANA_ID)));

        List<ListaResponseDTO> listas = service.obtenerTodas();

        assertThat(listas).hasSize(1);
        assertThat(listas.get(0).usuarioId()).isEqualTo(ANA_ID);
    }

    @Test
    void obtenerTodas_usuarioAdmin_devuelveTodasLasListas() {
        when(contextoSeguridad.esAdmin()).thenReturn(true);
        when(listaRepository.findAll()).thenReturn(List.of(
                new Lista(1L, "De Ana", ANA_ID),
                new Lista(2L, "De Bob", BOB_ID)
        ));

        List<ListaResponseDTO> listas = service.obtenerTodas();

        assertThat(listas).hasSize(2);
    }

    @Test
    void eliminar_listaAjena_lanzaAccesoDenegado() {
        when(listaRepository.findById(7L)).thenReturn(Optional.of(new Lista(7L, "De Ana", ANA_ID)));
        when(contextoSeguridad.usuarioActualId()).thenReturn(BOB_ID);
        when(contextoSeguridad.esAdmin()).thenReturn(false);

        assertThatThrownBy(() -> service.eliminar(7L))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void moverFavoritos_listasPropias_mueveYEliminaOrigen() {
        when(listaRepository.findById(1L)).thenReturn(Optional.of(new Lista(1L, "Origen", ANA_ID)));
        when(listaRepository.findById(2L)).thenReturn(Optional.of(new Lista(2L, "Destino", ANA_ID)));
        when(contextoSeguridad.usuarioActualId()).thenReturn(ANA_ID);
        when(favoritoRepository.findByListaId(1L)).thenReturn(List.of(
                new Favorito(100L, 1L, 5L, "Nota", LocalDateTime.now())
        ));

        service.moverFavoritos(1L, 2L);

        verify(listaRepository).deleteById(1L);
    }
}
