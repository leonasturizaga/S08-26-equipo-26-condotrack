import Navbar from '../components/Navbar.jsx'
import Hero from '../components/Hero.jsx'
import Footer from '../components/Footer.jsx'
import { useTranslation } from '../i18n/i18n.js'

function Home() {
  const { t } = useTranslation()

  return (
    <div className="public-page">
      <Navbar />

      <main>
        <Hero />

        <section className="feature-strip" id="features">
          <article className="feature-card">
            <span className="feature-number">01</span>
            <h2>{t('Unified operations')}</h2>
            <p>
              {t(
                'Connect the operational information of each building and unit in one place.'
              )}
            </p>
          </article>

          <article className="feature-card">
            <span className="feature-number">02</span>
            <h2>{t('Role-based access')}</h2>
            <p>
              {t(
                'Each role sees the modules and information appropriate to its responsibilities.'
              )}
            </p>
          </article>

          <article className="feature-card">
            <span className="feature-number">03</span>
            <h2>{t('Full traceability')}</h2>
            <p>
              {t(
                'Operational events are designed to remain linked to the building, unit and people involved.'
              )}
            </p>
          </article>
        </section>
      </main>

      <Footer />
    </div>
  )
}

export default Home