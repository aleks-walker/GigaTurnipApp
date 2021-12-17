package kg.kloop.android.gigaturnip.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kg.kloop.android.gigaturnip.data.models.mappers.CampaignDtoMapper
import kg.kloop.android.gigaturnip.data.models.mappers.NotificationDtoMapper
import kg.kloop.android.gigaturnip.data.models.mappers.TaskDtoMapper
import kg.kloop.android.gigaturnip.data.models.mappers.TaskStageDtoMapper
import kg.kloop.android.gigaturnip.data.remote.GigaTurnipApi
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepositoryImpl
import kg.kloop.android.gigaturnip.util.Constants.API_BASE_URL
import kg.kloop.android.gigaturnip.util.Constants.TEMP_AUDIO_FILE_NAME
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindRepository(
        repositoryImpl: GigaTurnipRepositoryImpl
    ): GigaTurnipRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideGigaTurnipRepository(
        api: GigaTurnipApi,
        campaignMapper: CampaignDtoMapper,
        tasksMapper: TaskDtoMapper,
        taskStageMapper: TaskStageDtoMapper,
        notificationDtoMapper: NotificationDtoMapper
    ) = GigaTurnipRepositoryImpl(
        api,
        campaignMapper,
        tasksMapper,
        taskStageMapper,
        notificationDtoMapper
    )

    @Singleton
    @Provides
    @Named("audioFilePath")
    fun provideAudioFilePath(
        @ApplicationContext context: Context
    ): String = "${context.filesDir?.absolutePath}/${TEMP_AUDIO_FILE_NAME}"      /* /data/data/com.example.gigaturnip/files/myRecord.wav  */

    @Singleton
    @Provides
    fun providesGigaTurnipApi(): GigaTurnipApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(API_BASE_URL)
            .build()
            .create(GigaTurnipApi::class.java)
    }

    @Singleton
    @Provides
    fun provideCampaignDtoMapper(): CampaignDtoMapper = CampaignDtoMapper()

    @Singleton
    @Provides
    fun provideTaskDtoMapper(): TaskDtoMapper = TaskDtoMapper()

    @Singleton
    @Provides
    fun provideTaskStageDtoMapper(): TaskStageDtoMapper = TaskStageDtoMapper()

    @Singleton
    @Provides
    fun provideNotificationDtoMapper(): NotificationDtoMapper = NotificationDtoMapper()

}