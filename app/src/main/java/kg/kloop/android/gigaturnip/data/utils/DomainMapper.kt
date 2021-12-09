package kg.kloop.android.gigaturnip.data.utils

import kg.kloop.android.gigaturnip.util.Resource

abstract class DomainMapper<T, DomainModel> {

    abstract fun mapToDomainModel(model: T): DomainModel

    abstract fun mapFromDomainModel(domainModel: DomainModel): T

    fun toDomainList(model: List<T>): List<DomainModel> {
        return model.map { mapToDomainModel(it) }
    }

    fun fromDomainList(domainModel: List<DomainModel>): List<T> {
        return domainModel.map { mapFromDomainModel(it) }
    }

}

suspend fun <T, DomainModel> getSingle(
    func: suspend () -> T,
    mapper: DomainMapper<T, DomainModel>
): Resource<DomainModel> {
    val response = try {
        func()
    } catch (e: Exception) {
        e.printStackTrace()
        return Resource.Error(e.message.toString())
    }
    return Resource.Success(mapper.mapToDomainModel(response))
}

suspend fun <T, DomainModel> getList(
    func: suspend () -> List<T>,
    mapper: DomainMapper<T, DomainModel>,
): Resource<List<DomainModel>> {
    val response = try {
        func()
    } catch (e: Exception) {
        e.printStackTrace()
        return Resource.Error(e.message.toString())
    }
    return Resource.Success(mapper.toDomainList(response))
}

