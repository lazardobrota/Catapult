package com.example.catapult.cats.db

//import com.example.catapult.cats.network.catapi
//import com.example.catapult.cats.network.resultsapi
import com.example.catapult.cats.db.images.CatGallery
import com.example.catapult.cats.db.images.CatGalleryDao
import com.example.catapult.cats.network.api.ICatListAPI
import com.example.catapult.cats.network.api.IResultsAPI
import com.example.catapult.cats.network.dto.ResultDTO
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatsService @Inject constructor(
    private val catDao: CatDao,
    private val catGalleryDao: CatGalleryDao,
    private val catApi: ICatListAPI,
    private val resultsApi: IResultsAPI
) {
    //ICatListAPI:: class.java -  reflection of ICatListAPI class and instance (reference type of java)
    //create validates interface
    //https://stackoverflow.com/questions/68686142/what-is-the-meaning-of-class-java
    //private val catApi: ICatListAPI = catapi.create(ICatListAPI:: class.java)
    //private val resultsApi: IResultsAPI = resultsapi.create(IResultsAPI:: class.java)

    suspend fun fetchAllCatsFromApi() {
        catDao.insertAllCats(cats = catApi.getAllCats())
    }

    fun getAllCatsFlow(): Flow<List<Cat>> = catDao.getAllCats()

    fun getCatByIdFlow(id: String): Flow<Cat> = catDao.getCatById(id)

    suspend fun getAllCatsPhotosApi(id: String): List<CatGallery> {
        val images = catApi.getAllCatsPhotos(id).map { it.copy(id = id) }
        catGalleryDao.insertAllGalleryCats(cats = images)
        return images
    }

    fun getAllCatImagesByIdFlow(id: String): Flow<List<String>> = catGalleryDao.getAllImagesForId(id)
    fun getCatImageByUrlFlow(url: String): Flow<String> = catGalleryDao.getImageByUrl(url)

    suspend fun fetchAllResultsForCategory(category: Int): List<ResultDTO> {
        return resultsApi.getAllResultsForCategory(category)
    }
     suspend fun postResult(nickname: String, result:Float, category: Int) {
        val dto = ResultDTO(nickname,result,category)
        resultsApi.postResult(dto)
    }

}