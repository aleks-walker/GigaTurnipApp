package kg.kloop.android.gigaturnip.data.models.mappers

import kg.kloop.android.gigaturnip.data.models.ChainDto
import kg.kloop.android.gigaturnip.data.utils.DomainMapper
import kg.kloop.android.gigaturnip.domain.Chain


class ChainDtoMapper : DomainMapper<ChainDto, Chain>() {
    override fun mapToDomainModel(model: ChainDto): Chain {
        return Chain(
            id = model.id,
            campaignId = model.campaignId,
            description = model.description,
            name = model.name
        )
    }

    override fun mapFromDomainModel(domainModel: Chain): ChainDto {
        return ChainDto(
            id = domainModel.id,
            campaignId = domainModel.campaignId,
            description = domainModel.description,
            name = domainModel.name
        )
    }
}