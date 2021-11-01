package kg.kloop.android.gigaturnip.data.models.mappers

import kg.kloop.android.gigaturnip.data.models.NotificationDto
import kg.kloop.android.gigaturnip.data.utils.DomainMapper
import kg.kloop.android.gigaturnip.domain.Notification

class NotificationDtoMapper : DomainMapper<NotificationDto, Notification>() {
    override fun mapToDomainModel(model: NotificationDto): Notification {
        return Notification(
            title = model.title,
            text = model.text,
            important = model.important
        )
    }

    override fun mapFromDomainModel(domainModel: Notification): NotificationDto {
        return NotificationDto(
            title = domainModel.title,
            text = domainModel.text,
            important = domainModel.important
        )
    }

}