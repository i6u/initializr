/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.initializr.web.project;

import java.util.Collections;

import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSystem;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.spring.test.InitializrMetadataTestBuilder;
import io.spring.initializr.generator.version.Version;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.Type;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link ProjectRequestToDescriptionConverter}.
 *
 * @author Madhura Bhave
 * @author Stephane Nicoll
 */
public class ProjectRequestToDescriptionConverterTests {

	private InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
			.build();

	private final ProjectRequestToDescriptionConverter converter = new ProjectRequestToDescriptionConverter();

	@Test
	public void convertWhenTypeIsInvalidShouldThrowException() {
		ProjectRequest request = getProjectRequest();
		request.setType("foo-build");
		assertThatExceptionOfType(InvalidProjectRequestException.class)
				.isThrownBy(() -> this.converter.convert(request, this.metadata))
				.withMessage("Unknown type 'foo-build' check project metadata");
	}

	@Test
	public void convertWhenTypeDoesNotDefineBuildTagShouldThrowException() {
		Type type = new Type();
		type.setId("example-project");
		InitializrMetadata testMetadata = InitializrMetadataTestBuilder.withDefaults()
				.addType(type).build();
		ProjectRequest request = getProjectRequest();
		request.setType("example-project");
		assertThatExceptionOfType(InvalidProjectRequestException.class)
				.isThrownBy(() -> this.converter.convert(request, testMetadata))
				.withMessage(
						"Invalid type 'example-project' (missing build tag) check project metadata");
	}

	@Test
	void convertWhenSpringBootVersionInvalidShouldThrowException() {
		ProjectRequest request = getProjectRequest();
		request.setBootVersion("1.2.3.M4");
		assertThatExceptionOfType(InvalidProjectRequestException.class)
				.isThrownBy(() -> this.converter.convert(request, this.metadata))
				.withMessage(
						"Invalid Spring Boot version 1.2.3.M4 must be 1.5.0 or higher");
	}

	@Test
	public void convertWhenPackagingIsInvalidShouldThrowException() {
		ProjectRequest request = getProjectRequest();
		request.setPackaging("star");
		assertThatExceptionOfType(InvalidProjectRequestException.class)
				.isThrownBy(() -> this.converter.convert(request, this.metadata))
				.withMessage("Unknown packaging 'star' check project metadata");
	}

	@Test
	public void convertWhenLanguageIsInvalidShouldThrowException() {
		ProjectRequest request = getProjectRequest();
		request.setLanguage("english");
		assertThatExceptionOfType(InvalidProjectRequestException.class)
				.isThrownBy(() -> this.converter.convert(request, this.metadata))
				.withMessage("Unknown language 'english' check project metadata");
	}

	@Test
	void convertWhenDependencyNotPresentShouldThrowException() {
		ProjectRequest request = getProjectRequest();
		request.setDependencies(Collections.singletonList("invalid"));
		assertThatExceptionOfType(InvalidProjectRequestException.class)
				.isThrownBy(() -> this.converter.convert(request, this.metadata))
				.withMessage("Unknown dependency 'invalid' check project metadata");
	}

	@Test
	void convertShouldSetApplicationNameForProjectDescriptionFromRequestWhenPresent() {
		ProjectRequest request = getProjectRequest();
		request.setApplicationName("MyApplication");
		ProjectDescription description = this.converter.convert(request, this.metadata);
		assertThat(description.getApplicationName()).isEqualTo("MyApplication");
	}

	@Test
	void convertShouldSetApplicationNameForProjectDescriptionUsingNameWhenAbsentFromRequest() {
		ProjectRequest request = getProjectRequest();
		ProjectDescription description = this.converter.convert(request, this.metadata);
		assertThat(description.getApplicationName()).isEqualTo("DemoApplication");
	}

	@Test
	void convertShouldSetGroupIdAndArtifactIdFromRequest() {
		ProjectRequest request = getProjectRequest();
		request.setArtifactId("foo");
		request.setGroupId("com.example");
		ProjectDescription description = this.converter.convert(request, this.metadata);
		assertThat(description.getGroupId()).isEqualTo("com.example");
		assertThat(description.getArtifactId()).isEqualTo("foo");
	}

	@Test
	void convertShouldSetBaseDirectoryFromRequest() {
		ProjectRequest request = getProjectRequest();
		request.setBaseDir("my-path");
		ProjectDescription description = this.converter.convert(request, this.metadata);
		assertThat(description.getBaseDirectory()).isEqualTo("my-path");
	}

	@Test
	void convertShouldSetBuildSystemFromRequestTypeAndBuildTag() {
		Type type = new Type();
		type.setId("example-type");
		type.getTags().put("build", "gradle");
		InitializrMetadata testMetadata = InitializrMetadataTestBuilder.withDefaults()
				.addType(type).build();
		ProjectRequest request = getProjectRequest();
		request.setType("example-type");
		ProjectDescription description = this.converter.convert(request, testMetadata);
		assertThat(description.getBuildSystem()).isInstanceOf(GradleBuildSystem.class);
	}

	@Test
	void convertShouldSetDescriptionFromRequest() {
		ProjectRequest request = getProjectRequest();
		request.setDescription("This is my demo project");
		ProjectDescription description = this.converter.convert(request, this.metadata);
		assertThat(description.getDescription()).isEqualTo("This is my demo project");
	}

	@Test
	void convertShouldSetPackagingFromRequest() {
		ProjectRequest request = getProjectRequest();
		request.setPackaging("war");
		ProjectDescription description = this.converter.convert(request, this.metadata);
		assertThat(description.getPackaging().id()).isEqualTo("war");
	}

	@Test
	void convertShouldSetPlatformVersionFromRequest() {
		ProjectRequest request = getProjectRequest();
		request.setBootVersion("2.0.3");
		ProjectDescription description = this.converter.convert(request, this.metadata);
		assertThat(description.getPlatformVersion()).isEqualTo(Version.parse("2.0.3"));
	}

	@Test
	void convertShouldUseDefaultPlatformVersionFromMetadata() {
		ProjectRequest request = getProjectRequest();
		ProjectDescription description = this.converter.convert(request, this.metadata);
		assertThat(description.getPlatformVersion())
				.isEqualTo(Version.parse("2.1.1.RELEASE"));
	}

	@Test
	void convertShouldSetLanguageForProjectDescriptionFromRequest() {
		ProjectRequest request = getProjectRequest();
		request.setJavaVersion("1.8");
		ProjectDescription description = this.converter.convert(request, this.metadata);
		assertThat(description.getLanguage().id()).isEqualTo("java");
		assertThat(description.getLanguage().jvmVersion()).isEqualTo("1.8");
	}

	private ProjectRequest getProjectRequest() {
		WebProjectRequest request = new WebProjectRequest();
		request.initialize(this.metadata);
		return request;
	}

}