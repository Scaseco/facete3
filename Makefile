.PHONY: release

RELEASE_REPO="target/release/faceted-browsing-benchmark-releases"

release: target/release/benchmark.ttl

target/release/benchmark.ttl: faceted-browsing-benchmark-parent/faceted-browsing-benchmark-v1-parent/faceted-browsing-benchmark-v1-core/src/main/resources/faceted-browsing-benchmark-v1-benchmark.ttl faceted-browsing-benchmark-parent/faceted-browsing-benchmark-v2-parent/faceted-browsing-benchmark-v2-core/src/main/resources/faceted-browsing-benchmark-v2-benchmark.ttl
	mkdir -p `dirname ${RELEASE_REPO}`
	git clone https://git.project-hobbit.eu/cstadler/faceted-browsing-benchmark-releases.git ${RELEASE_REPO} || true
	cat faceted-browsing-benchmark-parent/faceted-browsing-benchmark-v1-parent/faceted-browsing-benchmark-v1-core/src/main/resources/faceted-browsing-benchmark-v1-benchmark.ttl faceted-browsing-benchmark-parent/faceted-browsing-benchmark-v2-parent/faceted-browsing-benchmark-v2-core/src/main/resources/faceted-browsing-benchmark-v2-benchmark.ttl > ${RELEASE_REPO}/benchmark.ttl
	(cd ${RELEASE_REPO} && git add benchmark.ttl && git commit -m "Updated benchmark.ttl definition" && git push) || true

