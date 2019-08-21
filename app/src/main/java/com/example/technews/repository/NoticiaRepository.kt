package br.com.alura.technews.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import br.com.alura.technews.database.dao.NoticiaDAO
import br.com.alura.technews.model.Noticia
import br.com.alura.technews.retrofit.webclient.NoticiaWebClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoticiaRepository(
    private val dao: NoticiaDAO,
    private val webclient: NoticiaWebClient = NoticiaWebClient()
) {

    private val mediador = MediatorLiveData<Resource<List<Noticia>?>>()

    fun buscaTodos(): LiveData<Resource<List<Noticia>?>> {

        mediador.addSource(buscaInterno()) {
            mediador.value = Resource(dado = it)
        }

        val falhasDaWebApiLiveData = MutableLiveData<Resource<List<Noticia>?>>()
        mediador.addSource(falhasDaWebApiLiveData) { resourceDeFalha ->
            val resourceAtual = mediador.value
            val resourceNovo: Resource<List<Noticia>?> =
                if (resourceAtual != null)
                    Resource(dado = resourceAtual.dado, erro = resourceDeFalha.erro)
                else
                    resourceDeFalha

            mediador.value = resourceNovo
        }

        buscaNaApi()

        return mediador
    }

    fun salva(
        noticia: Noticia
    ): LiveData<Resource<Void?>> {
        val liveData = MutableLiveData<Resource<Void?>>()

        salvaNaApi(noticia)

        liveData.value = Resource(dado = null)
        return liveData
    }

    fun remove(
        noticia: Noticia
    ): LiveData<Resource<Void?>> {
        val liveData = MutableLiveData<Resource<Void?>>()

        removeNaApi(noticia)

        liveData.value = Resource(null)
        return liveData
    }

    fun edita(
        noticia: Noticia
    ): LiveData<Resource<Void?>> {
        val liveData = MutableLiveData<Resource<Void?>>()

        editaNaApi(noticia)

        liveData.value = Resource(null)
        return liveData
    }

    fun buscaPorId(
        noticiaId: Long
    ): LiveData<Noticia?> {
        return dao.buscaPorId(noticiaId)
    }

    private fun buscaNaApi() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            webclient.buscaTodas()?.let { noticiasNovas ->
                dao.salva(noticiasNovas)
            }
        }
    }

    private fun buscaInterno() : LiveData<List<Noticia>> {
        return dao.buscaTodos()
    }

    private fun salvaNaApi(noticia: Noticia){
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            webclient.salva(noticia)?.let { noticiaSalva ->
                dao.salva(noticiaSalva)
            }
        }
    }

    private fun removeNaApi(noticia: Noticia){
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            webclient.remove(noticia.id)?.let {
                dao.remove(noticia)
            }
        }
    }

    private fun editaNaApi(noticia: Noticia){
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            webclient.edita(noticia.id, noticia)?.let { noticiaEditada ->
                dao.salva(noticiaEditada)
            }
        }
    }
}
