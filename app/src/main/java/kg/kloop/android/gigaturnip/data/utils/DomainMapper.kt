package kg.kloop.android.gigaturnip.data.utils

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