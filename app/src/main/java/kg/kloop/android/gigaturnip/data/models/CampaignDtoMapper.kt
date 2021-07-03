package kg.kloop.android.gigaturnip.data.models

import kg.kloop.android.gigaturnip.data.utils.DomainMapper
import kg.kloop.android.gigaturnip.domain.Campaign

class CampaignDtoMapper : DomainMapper<CampaignDto, Campaign>(){
    override fun mapToDomainModel(model: CampaignDto): Campaign {
        return Campaign(
            id = model.id,
            title = model.name,
            description = model.description
        )
    }

    override fun mapFromDomainModel(domainModel: Campaign): CampaignDto {
        return CampaignDto(
            id = domainModel.id,
            name = domainModel.title,
            description = domainModel.description
        )
    }
}