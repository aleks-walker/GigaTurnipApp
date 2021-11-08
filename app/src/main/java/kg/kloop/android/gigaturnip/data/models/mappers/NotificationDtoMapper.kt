package kg.kloop.android.gigaturnip.data.models.mappers

import kg.kloop.android.gigaturnip.data.models.NotificationDto
import kg.kloop.android.gigaturnip.data.utils.DomainMapper
import kg.kloop.android.gigaturnip.domain.Notification

class NotificationDtoMapper : DomainMapper<NotificationDto, Notification>() {
    override fun mapToDomainModel(model: NotificationDto): Notification {
        return Notification(
            id = model.id,
            title = model.title,
            text = model.text,
            importance = model.importance,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt,
            campaignId = model.campaignId,
            rankId = model.rankId
        )
    }

    override fun mapFromDomainModel(domainModel: Notification): NotificationDto {
        return NotificationDto(
            id = domainModel.id,
            title = domainModel.title,
            text = domainModel.text,
            importance = domainModel.importance,
            createdAt = domainModel.createdAt,
            updatedAt = domainModel.updatedAt,
            campaignId = domainModel.campaignId,
            rankId = domainModel.rankId
        )
    }

}