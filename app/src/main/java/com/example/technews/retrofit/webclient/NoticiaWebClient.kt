package br.com.alura.technews.retrofit.webclient

import br.com.alura.technews.model.Noticia
import br.com.alura.technews.retrofit.AppRetrofit
import br.com.alura.technews.retrofit.service.NoticiaService

class NoticiaWebClient(
    private val service: NoticiaService = AppRetrofit().noticiaService
) {

    fun buscaTodas(): List<Noticia>?{
        return service.buscaTodas().execute().body()
    }

    fun salva(noticia: Noticia): Noticia?{
        return service.salva(noticia).execute().body()
    }

    fun edita(id: Long, noticia: Noticia): Noticia?{
        return service.edita(id, noticia).execute().body()
    }

    fun remove(id: Long){
        service.remove(id).execute()
    }

}
