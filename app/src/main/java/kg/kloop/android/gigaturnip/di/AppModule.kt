package kg.kloop.android.gigaturnip.di

import android.content.Context
import com.iceteck.silicompressorr.SiliCompressor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kg.kloop.android.gigaturnip.data.models.CampaignDtoMapper
import kg.kloop.android.gigaturnip.data.models.TaskDtoMapper
import kg.kloop.android.gigaturnip.data.models.TaskStageDtoMapper
import kg.kloop.android.gigaturnip.data.remote.GigaTurnipApi
import kg.kloop.android.gigaturnip.repository.GigaTurnipRepository
import kg.kloop.android.gigaturnip.util.Constants.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providesGigaTurnipRepository(
        api: GigaTurnipApi
    ) = GigaTurnipRepository(
        api,
        CampaignDtoMapper(),
        TaskDtoMapper(),
        TaskStageDtoMapper()
    )

    @Singleton
    @Provides
    fun providesGigaTurnipApi(): GigaTurnipApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(GigaTurnipApi::class.java)

    }

    @Singleton
    @Provides
    fun provideCampaignDtoMapper(): CampaignDtoMapper {
        return CampaignDtoMapper()
    }

    @Singleton
    @Provides
    fun provideTaskDtoMapper(): TaskDtoMapper {
        return TaskDtoMapper()
    }

    @Singleton
    @Provides
    fun provideTaskStageDtoMapper(): TaskStageDtoMapper {
        return TaskStageDtoMapper()
    }

    @Singleton
    @Provides
    fun provideVideoCompressor(@ApplicationContext context: Context): SiliCompressor {
        return SiliCompressor.with(context)
    }

}